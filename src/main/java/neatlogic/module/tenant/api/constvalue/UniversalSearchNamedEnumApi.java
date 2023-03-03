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

package neatlogic.module.tenant.api.constvalue;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.EnumFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;


@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class UniversalSearchNamedEnumApi extends PrivateApiComponentBase {
    @Override
    public String getToken() {
        return "universal/enum/search";
    }

    @Override
    public String getName() {
        return "搜索具名枚举";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "keyword", type = ApiParamType.STRING, desc = "枚举名称")})
    @Output({@Param(name = "name", type = ApiParamType.STRING, desc = "名称"),
            @Param(name = "className", type = ApiParamType.STRING, desc = "类路径")})
    @Description(desc = "搜索具名枚举接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        return EnumFactory.searchEnumClassByName(jsonObj.getString("keyword"));
    }
}
