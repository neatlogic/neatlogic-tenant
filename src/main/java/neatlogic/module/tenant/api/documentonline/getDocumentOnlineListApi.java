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
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.framework.startup.InitializeIndexHandler;
import org.springframework.stereotype.Service;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class getDocumentOnlineListApi extends PrivateApiComponentBase {

    @Override
    public String getName() {
        return "查询在线帮助文档列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "moduleGroup", type = ApiParamType.STRING, isRequired = true, desc = "模块组标识"),
            @Param(name = "menu", type = ApiParamType.STRING, desc = "菜单标识"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数据条目")
    })
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "文档列表")
    })
    @Description(desc = "查询在线帮助文档")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        BasePageVo basePageVo = paramObj.toJavaObject(BasePageVo.class);
        String moduleGroup = paramObj.getString("moduleGroup");
        String menu = paramObj.getString("menu");
        return InitializeIndexHandler.getDocumentOnlineList(moduleGroup, menu, basePageVo);
    }

    @Override
    public String getToken() {
        return "documentonline/list";
    }
}
