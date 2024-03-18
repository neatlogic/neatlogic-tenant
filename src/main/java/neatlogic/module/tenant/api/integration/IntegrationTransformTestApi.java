/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

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
