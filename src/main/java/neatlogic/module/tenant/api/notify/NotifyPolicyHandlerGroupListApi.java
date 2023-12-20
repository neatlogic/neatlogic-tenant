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

package neatlogic.module.tenant.api.notify;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.notify.dto.NotifyTreeVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

//@Service
@Deprecated
@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyPolicyHandlerGroupListApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "notify/policy/handlergroup/list";
    }

    @Override
    public String getName() {
        return "获取通知策略分类分组列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({
            @Param(explode = NotifyTreeVo[].class, desc = "通知策略分类列表")
    })
    @Description(desc = "获取通知策略分类分组列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
//        return NotifyPolicyHandlerFactory.getModuleTreeVoList();
        return null;
    }

}
