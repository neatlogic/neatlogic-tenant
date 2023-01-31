/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.tenant.api.file;

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.FileUtil;
import neatlogic.framework.exception.file.*;
import neatlogic.framework.exception.user.NoTenantException;
import neatlogic.framework.file.core.FileTypeHandlerFactory;
import neatlogic.framework.file.core.IFileTypeHandler;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileTypeVo;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.module.framework.file.handler.LocalFileSystemHandler;
import neatlogic.module.framework.file.handler.MinioFileSystemHandler;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class UploadFileApi extends PrivateBinaryStreamApiComponentBase {
    static Logger logger = LoggerFactory.getLogger(UploadFileApi.class);


    @Autowired
    private FileMapper fileMapper;

    @Override
    public String getToken() {
        return "file/upload";
    }

    @Override
    public String getName() {
        return "附件上传接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "param", type = ApiParamType.STRING, desc = "附件参数名称", isRequired = true),
            @Param(name = "type", type = ApiParamType.STRING, desc = "附件类型", isRequired = true),
            @Param(name = "uniqueKey", type = ApiParamType.STRING, desc = "当附件名称需要唯一时需要提供，相同uniqueKey值的附件名称不能重复")})
    @Output({@Param(explode = FileVo.class)})
    @Description(desc = "附件上传接口")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String tenantUuid = TenantContext.get().getTenantUuid();
        if (StringUtils.isBlank(tenantUuid)) {
            throw new NoTenantException();
        }
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        String paramName = paramObj.getString("param");
        String type = paramObj.getString("type");
        String uniqueKey = paramObj.getString("uniqueKey");
        List<FileTypeVo> fileTypeList = FileTypeHandlerFactory.getActiveFileTypeHandler();
        FileTypeVo fileTypeVo = null;
        for (FileTypeVo f : fileTypeList) {
            if (f.getName().equalsIgnoreCase(type)) {
                fileTypeVo = f;
                break;
            }
        }
        if (fileTypeVo == null) {
            throw new FileTypeHandlerNotFoundException(type);
        }
        FileTypeVo fileTypeConfigVo = fileMapper.getFileTypeConfigByType(fileTypeVo.getName());

        MultipartFile multipartFile = multipartRequest.getFile(paramName);

        if (multipartFile != null) {
            IFileTypeHandler fileTypeHandler = FileTypeHandlerFactory.getHandler(type);
            if (fileTypeHandler == null) {
                throw new FileTypeHandlerNotFoundException(type);
            }
            if (fileTypeHandler.needSave() && fileTypeHandler.isUnique() && StringUtils.isBlank(uniqueKey)) {
                throw new FileUniqueKeyEmptyException();
            }
            if (fileTypeHandler.beforeUpload(paramObj)) {
                multipartFile.getName();
                String userUuid = UserContext.get().getUserUuid(true);
                String oldFileName = multipartFile.getOriginalFilename();
                long size = multipartFile.getSize();
                // 如果配置为空代表不受任何限制
                if (fileTypeConfigVo != null) {
                    boolean isAllowed = false;
                    long maxSize = 0L;
                    String fileExt = "";
                    if (StringUtils.isNotBlank(oldFileName)) {
                        fileExt = oldFileName.substring(oldFileName.lastIndexOf(".") + 1).toLowerCase();
                    }
                    JSONObject configObj = fileTypeConfigVo.getConfigObj();
                    JSONArray whiteList = new JSONArray();
                    JSONArray blackList = new JSONArray();
                    if (size == 0) {
                        throw new EmptyFileException();
                    }
                    if (configObj != null) {
                        whiteList = configObj.getJSONArray("whiteList");
                        blackList = configObj.getJSONArray("blackList");
                        maxSize = configObj.getLongValue("maxSize");
                    }
                    if (whiteList != null && whiteList.size() > 0) {
                        for (int i = 0; i < whiteList.size(); i++) {
                            if (fileExt.equalsIgnoreCase(whiteList.getString(i))) {
                                isAllowed = true;
                                break;
                            }
                        }
                    } else if (blackList != null && blackList.size() > 0) {
                        isAllowed = true;
                        for (int i = 0; i < blackList.size(); i++) {
                            if (fileExt.equalsIgnoreCase(blackList.getString(i))) {
                                isAllowed = false;
                                break;
                            }
                        }
                    } else {
                        isAllowed = true;
                    }
                    if (!isAllowed) {
                        throw new FileExtNotAllowedException(fileExt);
                    }
                    if (maxSize > 0 && size > maxSize) {
                        throw new FileTooLargeException(size, maxSize);
                    }
                }


                FileVo fileVo = new FileVo();
                fileVo.setName(oldFileName);
                fileVo.setSize(size);
                fileVo.setUserUuid(userUuid);
                fileVo.setType(type);
                fileVo.setContentType(multipartFile.getContentType());
                if (fileTypeHandler.needSave()) {
                    FileVo oldFileVo = null;
                    if (fileTypeHandler.isUnique()) {
                        String uk = fileTypeHandler.getUniqueKey(uniqueKey);
                        fileVo.setUniqueKey(uk);
                        oldFileVo = fileMapper.getFileByNameAndUniqueKey(fileVo.getName(), uk);
                    }
                    String filePath;
                    try {
                        filePath = FileUtil.saveData(MinioFileSystemHandler.NAME, tenantUuid, multipartFile.getInputStream(), fileVo.getId().toString(), fileVo.getContentType(), fileVo.getType());
                    } catch (Exception ex) {
                        //如果没有配置minioUrl，则表示不使用minio，无需抛异常
                        if (StringUtils.isNotBlank(Config.MINIO_URL())) {
                            logger.error(ex.getMessage(), ex);
                        }
                        // 如果minio出现异常，则上传到本地
                        filePath = FileUtil.saveData(LocalFileSystemHandler.NAME, tenantUuid, multipartFile.getInputStream(), fileVo.getId().toString(), fileVo.getContentType(), fileVo.getType());
                    }
                    fileVo.setPath(filePath);
                    if (oldFileVo == null) {
                        fileMapper.insertFile(fileVo);
                    } else {
                        FileUtil.deleteData(oldFileVo.getPath());
                        fileVo.setId(oldFileVo.getId());
                        fileMapper.updateFile(fileVo);
                    }
                    fileTypeHandler.afterUpload(fileVo, paramObj);
                    FileVo file = fileMapper.getFileById(fileVo.getId());
                    file.setUrl("api/binary/file/download?id=" + fileVo.getId());

                    return file;
                } else {
                    fileTypeHandler.analyze(multipartFile, paramObj);
                    return fileVo;
                }
            }
        }
        return null;
    }

}
