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

package neatlogic.module.tenant.api.documentonline;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.DOCUMENTONLINE_CONFIG_MODIFY;
import neatlogic.framework.documentonline.dto.DocumentOnlineConfigVo;
import neatlogic.framework.documentonline.dto.DocumentOnlineVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.util.FileUtil;
import neatlogic.module.framework.startup.DocumentOnlineInitializeIndexHandler;
import neatlogic.module.tenant.service.documentonline.DocumentOnlineService;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = DOCUMENTONLINE_CONFIG_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ExportDocumentOnlineConfigApi extends PrivateBinaryStreamApiComponentBase {
    @Resource
    private DocumentOnlineService documentOnlineService;

    @Override
    public String getName() {
        return "nmtad.exportdocumentonlineconfigapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({})
    @Description(desc = "nmtad.exportdocumentonlineconfigapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<DocumentOnlineConfigVo> allList = new ArrayList<>();
        List<DocumentOnlineVo> allFileList = documentOnlineService.getAllFileList(DocumentOnlineInitializeIndexHandler.DOCUMENT_ONLINE_DIRECTORY_ROOT);
        for (DocumentOnlineVo documentOnlineVo : allFileList) {
            List<DocumentOnlineConfigVo> configList = documentOnlineVo.getConfigList();
            for (DocumentOnlineConfigVo configVo : configList) {
                configVo.setSource(null);
                allList.add(configVo);
            }
        }
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", " attachment; filename=\"" + FileUtil.getEncodedFileName("documentonline-mapping.json") + "\"");
        try (InputStream in = IOUtils.toInputStream(JSONObject.toJSONString(allList), StandardCharsets.UTF_8);
             ServletOutputStream os = response.getOutputStream()) {
            IOUtils.copyLarge(in, os);
            os.flush();
        }
        return null;
    }

    @Override
    public String getToken() {
        return "documentonline/config/export";
    }
}
