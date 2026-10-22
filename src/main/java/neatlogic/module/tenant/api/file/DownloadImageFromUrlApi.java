/*
 * Copyright (C) 2026 TechSure Co., Ltd. All Rights Reserved.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 */
package neatlogic.module.tenant.api.file;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.FileUtil;
import neatlogic.framework.exception.file.UploadFileFailedException;
import neatlogic.framework.exception.user.NoTenantException;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileTypeVo;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.util.NetworkImageDownloader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.io.InputStream;
import java.nio.file.Files;

/** 将公网图片下载到现有附件存储，并登记当前租户的附件信息。 */
@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.CREATE)
@Transactional(rollbackFor = Exception.class)
public class DownloadImageFromUrlApi extends PrivateApiComponentBase {
    private static final Logger logger = LoggerFactory.getLogger(DownloadImageFromUrlApi.class);

    @Resource
    private FileMapper fileMapper;

    /** 使用独立 token，避免与已有图片二进制下载接口冲突。 */
    @Override
    public String getToken() {
        return "image/download/from/url";
    }

    /** 返回接口名称，供接口管理和帮助信息展示。 */
    @Override
    public String getName() {
        return "下载网络图片到服务器";
    }

    /** 本接口不需要额外配置页面。 */
    @Override
    public String getConfig() {
        return null;
    }

    /** 下载并保存图片，只有文件保存成功后才写入附件记录。 */
    @Input({@Param(name = "url", type = ApiParamType.STRING, isRequired = true, desc = "公网图片地址")})
    @Output({@Param(explode = FileVo.class)})
    @Description(desc = "通过公网 HTTP/HTTPS 图片地址保存附件并返回文件信息")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String tenantUuid = TenantContext.get().getTenantUuid();
        if (StringUtils.isBlank(tenantUuid)) {
            throw new NoTenantException();
        }
        String userUuid = UserContext.get().getUserUuid(true);
        FileTypeVo typeConfig = fileMapper.getFileTypeConfigByType("image");
        JSONObject config = typeConfig == null ? null : typeConfig.getConfigObj();
        try (NetworkImageDownloader.DownloadedImage image = createDownloader().download(paramObj.getString("url"), config)) {
            FileVo fileVo = new FileVo();
            fileVo.setName(image.getName());
            fileVo.setSize(image.getSize());
            fileVo.setUserUuid(userUuid);
            fileVo.setType("image");
            fileVo.setContentType(image.getContentType());
            String filePath;
            try (InputStream inputStream = Files.newInputStream(image.getFile())) {
                filePath = FileUtil.saveData(tenantUuid, inputStream, fileVo);
            }
            if (StringUtils.isBlank(filePath)) {
                throw new UploadFileFailedException("图片保存路径为空");
            }
            fileVo.setPath(filePath);
            boolean cleanupRegistered = false;
            try {
                // 文件存储不参与数据库事务，事务未提交时需补偿删除已保存的文件。
                if (TransactionSynchronizationManager.isActualTransactionActive()
                        && TransactionSynchronizationManager.isSynchronizationActive()) {
                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        /** 事务明确回滚时清理文件，提交结果未知时保留文件以便排查。 */
                        @Override
                        public void afterCompletion(int status) {
                            if (status == STATUS_ROLLED_BACK) {
                                cleanupFile(filePath);
                            }
                        }
                    });
                    cleanupRegistered = true;
                }
                fileMapper.insertFile(fileVo);
                fileVo.setUrl("api/binary/image/download?id=" + fileVo.getId());
                return fileVo;
            } catch (Exception | Error ex) {
                // 直接调用或事务同步不可用时，立即补偿且不覆盖原始入库异常。
                if (!cleanupRegistered) {
                    cleanupFile(filePath);
                }
                throw ex;
            }
        }
    }

    /** 创建下载器，保持网络下载与接口持久化职责分离。 */
    protected NetworkImageDownloader createDownloader() {
        return new NetworkImageDownloader();
    }

    /** 补偿删除失败仅记录日志，保留导致请求失败的原始异常。 */
    private void cleanupFile(String filePath) {
        try {
            FileUtil.deleteData(filePath);
        } catch (Exception ex) {
            logger.error("清理网络图片失败，存储路径：{}", filePath, ex);
        }
    }
}
