/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tenant.api.integration;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.integration.IntegrationNotFoundException;
import neatlogic.framework.integration.dao.mapper.IntegrationMapper;
import neatlogic.framework.integration.dto.IntegrationVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.util.FileUtil;
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
