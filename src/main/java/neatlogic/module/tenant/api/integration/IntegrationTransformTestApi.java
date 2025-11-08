/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.api.integration;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.integration.ParamFormatInvalidException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.javascript.JavascriptUtil;
import org.springframework.stereotype.Service;

import java.io.StringWriter;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class IntegrationTransformTestApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "integration/transformtest";
    }

    @Override
    public String getName() {
        return "集成设置参数转换测试";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "content", type = ApiParamType.STRING, desc = "原始内容，需要符合json格式", isRequired = true), @Param(name = "template", type = ApiParamType.STRING, desc = "转换模板，如果为空则不做转换")})
    @Output({@Param(name = "Return", type = ApiParamType.STRING, desc = "返回结果")})
    @Description(desc = "集成设置参数转换测试")
    @Override
    public Object myDoService(JSONObject jsonObj) {
        String content = jsonObj.getString("content");
        String template = jsonObj.getString("template");
        Object object = null;
        try {
            object = JSON.parseObject(content);
        } catch (Exception ex) {
            try {
                object = JSON.parseArray(content);
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
