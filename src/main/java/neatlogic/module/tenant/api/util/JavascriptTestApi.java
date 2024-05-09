/*
 * Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neatlogic.module.tenant.api.util;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.javascript.JavascriptUtil;
import org.springframework.stereotype.Service;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class JavascriptTestApi extends PrivateApiComponentBase {
    @Override
    public String getName() {
        return "测试JavaScript脚本执行";
    }

    @Input({
            @Param(name = "param", type = ApiParamType.STRING, isRequired = true, desc = "参数"),
            @Param(name = "script", type = ApiParamType.STRING, isRequired = true, desc = "脚本")
    })
    @Output({
            @Param(name = "result", type = ApiParamType.BOOLEAN)
    })
    @Description(desc = "")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject param = jsonObj.getJSONObject("param");
        String script = jsonObj.getString("script");
        JSONObject paramObj = new JSONObject();
        paramObj.put("data", param);
        System.out.println("paramObj = " + paramObj);
        Object returnValue = JavascriptUtil.runScript(paramObj, script);
        Boolean result = Boolean.parseBoolean(returnValue.toString());
        JSONObject resultObj = new JSONObject();
        resultObj.put("result", result);
        return resultObj;
    }

    @Override
    public String getToken() {
        return "util/javascript";
    }
}
