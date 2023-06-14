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
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.framework.startup.DocumentOnlineInitializeIndexHandler;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class getDocumentOnlineTableListApi extends PrivateApiComponentBase {

    @Override
    public String getName() {
        return "查询在线帮助文档列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数据条目")
    })
    @Output({
            @Param(name = "tableList", type = ApiParamType.JSONARRAY, desc = "文档列表")
    })
    @Description(desc = "查询在线帮助文档")
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
                List<JSONObject> tbodyList = getAllFileList(firstLevelDirectory);
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


    /**
     * 通过递归，获取某个目录下的所有文件
     * @param directory
     * @return
     */
    private static List<JSONObject> getAllFileList(DocumentOnlineDirectoryVo directory) {
        List<JSONObject> list = new ArrayList<>();
        for (DocumentOnlineDirectoryVo child : directory.getChildren()) {
            if (child.getIsFile()) {
                JSONObject fileInfo = new JSONObject();
                fileInfo.put("upwardNameList", child.getUpwardNameList());
                fileInfo.put("filePath", child.getFilePath());
                fileInfo.put("fileName", child.getName());
                list.add(fileInfo);
            } else {
                list.addAll(getAllFileList(child));
            }
        }
        return list;
    }
}
