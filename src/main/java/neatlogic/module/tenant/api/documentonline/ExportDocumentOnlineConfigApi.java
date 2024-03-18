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
