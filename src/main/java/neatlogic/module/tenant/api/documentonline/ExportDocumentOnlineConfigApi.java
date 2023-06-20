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
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.documentonline.crossover.IDocumentOnlineCrossoverMapper;
import neatlogic.framework.documentonline.dto.DocumentOnlineConfigVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.util.FileUtil;
import neatlogic.module.framework.startup.DocumentOnlineInitializeIndexHandler;
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
@OperationType(type = OperationTypeEnum.SEARCH)
public class ExportDocumentOnlineConfigApi extends PrivateBinaryStreamApiComponentBase {
    @Override
    public String getName() {
        return "导出在线帮助文档配置文件";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({})
    @Description(desc = "导出在线帮助文档配置文件")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<DocumentOnlineConfigVo> allList = new ArrayList<>();
        IDocumentOnlineCrossoverMapper documentOnlineCrossoverMapper = CrossoverServiceFactory.getApi(IDocumentOnlineCrossoverMapper.class);
        // 先查询出数据库中数据
        List<DocumentOnlineConfigVo> documentOnlineConfigList = documentOnlineCrossoverMapper.getAllDocumentOnlineConfigList();
        for (DocumentOnlineConfigVo documentOnlineConfigVo : documentOnlineConfigList) {
            documentOnlineConfigVo.setSource("database");
            allList.add(documentOnlineConfigVo);
        }
        // 再查询出配置文件中数据
        for (DocumentOnlineConfigVo documentOnlineConfigVo : DocumentOnlineInitializeIndexHandler.getMappingConfigList()) {
            // 如果配置文件中数据的主键与数据库中数据的主键相同，则数据库中数据优先级较高
            if (!allList.contains(documentOnlineConfigVo)) {
                allList.add(documentOnlineConfigVo);
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
