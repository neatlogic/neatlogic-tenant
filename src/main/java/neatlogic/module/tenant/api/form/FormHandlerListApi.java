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

package neatlogic.module.tenant.api.form;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.form.attribute.core.FormAttributeHandlerFactory;
import neatlogic.framework.form.attribute.core.IFormAttributeHandler;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class FormHandlerListApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "form/handler/list";
    }

    @Override
    public String getName() {
        return "获取表单组件列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({@Param(name = "handler", type = ApiParamType.STRING, desc = "处理器"),
            @Param(name = "name", type = ApiParamType.STRING, desc = "处理器中文名"),
            @Param(name = "icon", type = ApiParamType.STRING, desc = "图标"),
            @Param(name = "type", type = ApiParamType.ENUM, desc = "分类，form（表单组件）|control（控制组件）"),
            @Param(name = "isConditionable", type = ApiParamType.INTEGER, desc = "是否可设置为条件"),
            @Param(name = "isExtendable", type = ApiParamType.ENUM, desc = "是否有拓展属性"),
            @Param(name = "isFilterable", type = ApiParamType.ENUM, desc = "是否可设置过滤"),
            @Param(name = "isShowable", type = ApiParamType.ENUM, desc = "是否可设置显示隐藏"),
            @Param(name = "isValueable", type = ApiParamType.ENUM, desc = "是否可设置赋值"),
            @Param(name = "module", type = ApiParamType.ENUM, desc = "属模块"),
    })
    @Description(desc = "获取表单组件列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray jsonArray = new JSONArray();
        for(IFormAttributeHandler handler : FormAttributeHandlerFactory.getHandlerList()){
            JSONObject json = new JSONObject();
            json.put("handler", handler.getHandler());
            json.put("name", handler.getHandlerName());
            json.put("icon", handler.getIcon());
            json.put("type", handler.getType());
            json.put("isConditionable", handler.isConditionable());
            json.put("isExtendable", handler.isExtendable());
            json.put("isFilterable", handler.isFilterable());
            json.put("isShowable", handler.isShowable());
            json.put("isValueable", handler.isValueable());
            json.put("module", handler.getModule());
            jsonArray.add(json);
        }
        return jsonArray;
    }
}
