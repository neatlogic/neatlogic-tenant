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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.RequestContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.documentonline.dto.DocumentOnlineDirectoryVo;
import neatlogic.framework.documentonline.dto.DocumentOnlineVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.framework.startup.DocumentOnlineInitializeIndexHandler;
import neatlogic.module.tenant.service.documentonline.DocumentOnlineService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetDocumentOnlineTableListApi extends PrivateApiComponentBase {

    @Resource
    private DocumentOnlineService documentOnlineService;

    @Override
    public String getName() {
        return "nmtad.getdocumentonlinetablelistapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize")
    })
    @Output({
            @Param(name = "tableList", type = ApiParamType.JSONARRAY, desc = "common.tablelist")
    })
    @Description(desc = "nmtad.getdocumentonlinetablelistapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONArray tableList = new JSONArray();
        Locale locale = RequestContext.get() != null ? RequestContext.get().getLocale() : Locale.getDefault();
        for (DocumentOnlineDirectoryVo localeLevel : DocumentOnlineInitializeIndexHandler.DOCUMENT_ONLINE_DIRECTORY_ROOT.getChildren()) {
            if (!Objects.equals(localeLevel.getName(), locale.getLanguage())) {
                continue;
            }
            for (DocumentOnlineDirectoryVo firstLevelDirectory : localeLevel.getChildren()) {
                JSONObject tableObj = new JSONObject();
                tableObj.put("firstLevelDirectory", firstLevelDirectory.getName());
                List<DocumentOnlineVo> tbodyList = documentOnlineService.getAllFileList(firstLevelDirectory);
                BasePageVo basePageVo = paramObj.toJavaObject(BasePageVo.class);
                basePageVo.setRowNum(tbodyList.size());
                tableObj.put("currentPage", basePageVo.getCurrentPage());
                tableObj.put("pageSize", basePageVo.getPageSize());
                tableObj.put("pageCount", basePageVo.getPageCount());
                tableObj.put("rowNum", basePageVo.getRowNum());
                tbodyList = PageUtil.subList(tbodyList, basePageVo);
                tableObj.put("tbodyList", tbodyList);
                tableList.add(tableObj);
            }
        }
        JSONObject resultObj = new JSONObject();
        resultObj.put("tableList", tableList);
        return resultObj;
    }

    @Override
    public String getToken() {
        return "documentonline/table/list";
    }
}
