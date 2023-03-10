/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        return "????????????????????????";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "uuidList", type = ApiParamType.JSONARRAY, desc = "????????????uuid??????", isRequired = true)})
    @Description(desc = "????????????????????????")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<String> uuidList = paramObj.getJSONArray("uuidList").toJavaList(String.class);
        List<String> existedUuidList = integrationMapper.checkUuidListExists(uuidList);
        uuidList.removeAll(existedUuidList);
        if (CollectionUtils.isNotEmpty(uuidList)) {
            throw new IntegrationNotFoundException(uuidList);
        }
        String fileName = FileUtil.getEncodedFileName("????????????." + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".pak");
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
