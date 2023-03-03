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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import neatlogic.framework.auth.core.AuthAction;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.restful.core.publicapi.PublicApiComponentFactory;
import neatlogic.framework.restful.dto.ApiHandlerVo;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class ApiManageApiHandlerListForSelectApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "apimanage/apihandler/list/forselect";
    }

    @Override
    public String getName() {
        return "获取接口组件列表_下拉框";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({})
    @Output({@Param(explode = ValueTextVo[].class, desc = "接口组件列表")})
    @Description(desc = "获取接口组件列表_下拉框")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {

        List<ValueTextVo> resultList = new ArrayList<>();

        List<ApiHandlerVo> apiHandlerList = PublicApiComponentFactory.getApiHandlerList();
        
        apiHandlerList.sort(Comparator.comparing(ApiHandlerVo::getHandler));

        for(ApiHandlerVo vo : apiHandlerList){
            resultList.add(new ValueTextVo(vo.getHandler(),vo.getName()));
        }

        return resultList;

    }

}
