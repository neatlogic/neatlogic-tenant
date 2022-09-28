/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.integration;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.integration.IntegrationNotFoundException;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.framework.util.FileUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class IntegrationExportApi extends PrivateBinaryStreamApiComponentBase {

    @Resource
    private IntegrationMapper integrationMapper;

    @Override
    public String getToken() {
        return "integration/export";
    }

    @Override
    public String getName() {
        return "导出集成设置信息";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "uuidList", type = ApiParamType.JSONARRAY, desc = "集成配置uuid列表", isRequired = true)})
    @Description(desc = "导出集成设置信息")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<String> uuidList = paramObj.getJSONArray("uuidList").toJavaList(String.class);
        List<String> existedUuidList = integrationMapper.checkUuidListExists(uuidList);
        uuidList.removeAll(existedUuidList);
        if (CollectionUtils.isNotEmpty(uuidList)) {
            throw new IntegrationNotFoundException(uuidList);
        }
        String fileName = FileUtil.getEncodedFileName("集成配置." + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".pak");
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", " attachment; filename=\"" + fileName + "\"");
        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            for (String uuid : existedUuidList) {
                IntegrationVo integration = integrationMapper.getIntegrationByUuid(uuid);
                zos.putNextEntry(new ZipEntry(integration.getName() + ".json"));
                zos.write(JSONObject.toJSONBytes(integration));
                zos.closeEntry();
            }
        }

        return null;
    }

}
