/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.api.documentonline;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.DOCUMENTONLINE_CONFIG_MODIFY;
import neatlogic.framework.documentonline.dto.DocumentOnlineConfigVo;
import neatlogic.framework.documentonline.dto.DocumentOnlineVo;
import neatlogic.framework.documentonline.util.DocumentOnlineManager;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.util.FileUtil;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

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
        List<DocumentOnlineVo> allFileList = DocumentOnlineManager.getAllFileList(DocumentOnlineManager.getDocumentOnlineDirectoryRoot());
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
