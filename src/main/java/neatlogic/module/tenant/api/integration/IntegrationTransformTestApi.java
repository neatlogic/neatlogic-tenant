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

package neatlogic.module.tenant.api.integration;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.integration.ParamFormatInvalidException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.javascript.JavascriptUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import java.io.StringWriter;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class IntegrationTransformTestApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "integration/transformtest";
    }

    @Override
    public String getName() {
        return "集成设置参数转换测试接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "content", type = ApiParamType.STRING, desc = "原始内容，需要符合json格式", isRequired = true), @Param(name = "template", type = ApiParamType.STRING, desc = "转换模板，如果为空则不做转换")})
    @Output({@Param(name = "Return", type = ApiParamType.STRING, desc = "返回结果")})
    @Description(desc = "集成设置参数转换测试接口")
    @Override
    public Object myDoService(JSONObject jsonObj) {
        String content = jsonObj.getString("content");
        String template = jsonObj.getString("template");
        Object object = null;
        try {
            object = JSONObject.parseObject(content);
        } catch (Exception ex) {
            try {
                object = JSONArray.parseArray(content);
            } catch (Exception ignored) {

            }
        }
        if (object == null) {
            throw new ParamFormatInvalidException();
        }
        JSONObject returnObj = new JSONObject();
        String returnStr;
        try {
            StringWriter sw = new StringWriter();
            returnStr = JavascriptUtil.transform(object, template, sw);
            returnObj.put("result", returnStr);
            returnObj.put("output", sw.toString());
        } catch (Exception e) {
            returnObj.put("error", e.getMessage());
        }

        return returnObj;
    }
}
