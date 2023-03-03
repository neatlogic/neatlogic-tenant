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

package neatlogic.module.tenant.api.apimanage;

import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.auth.core.ApiAuthFactory;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.restful.enums.PublicApiAuthType;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

@Deprecated
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ApiManageAuthTypeListForSelectApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "apimanage/authtype/list/forselect";
    }

    @Override
    public String getName() {
        return "获取接口组件认证方式列表_下拉框";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({})
    @Output({@Param(explode = ValueTextVo[].class, desc = "组件认证方式列表")})
    @Description(desc = "获取接口组件认证方式列表_下拉框")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray resultList = new JSONArray();
        for (PublicApiAuthType s : PublicApiAuthType.values()) {
            JSONObject json = new JSONObject();
            json.put("value", s.getValue());
            json.put("text", s.getText());
            json.put("help", ApiAuthFactory.getApiAuth(s.getValue()).help());
            resultList.add(json);
        }
        return resultList;
    }

}
