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

package neatlogic.module.tenant.api.module;

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.MODULE_MODIFY;
import neatlogic.framework.dto.module.ModuleGroupVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@AuthAction(action = MODULE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchModuleApi extends PrivateApiComponentBase {


    @Override
    public String getToken() {
        return "/module/search";
    }

    @Override
    public String getName() {
        return "获取模块列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({@Param(explode = ModuleGroupVo[].class)})
    @Description(desc = "获取模块列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<ModuleGroupVo> moduleGroupList = TenantContext.get().getActiveModuleGroupList();
        moduleGroupList.sort(Comparator.comparing(ModuleGroupVo::getGroupSort));
        return moduleGroupList;
    }
}
