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

package neatlogic.module.tenant.api.util;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.StringWriter;
import java.io.Writer;

/**
 * @author linbq
 * @since 2021/8/26 11:55
 **/
@Deprecated// 有安全问题
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class FreeMarkerTransformApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "util/freeMarker/transform";
    }

    @Override
    public String getName() {
        return "freeMarker测试工具";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "freeMarkerCode", type = ApiParamType.STRING, isRequired = true, desc = "freeMarker模板"),
            @Param(name = "JSONCode", type = ApiParamType.JSONOBJECT, isRequired = true, desc = "JSON对象")
    })
    @Output({
            @Param(name = "Return", type = ApiParamType.STRING, desc = "结果或错误信息")
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String content = paramObj.getString("freeMarkerCode");
        JSONObject dataObj = paramObj.getJSONObject("JSONCode");
        try {
            if (content != null) {
                Configuration cfg = new Configuration(Configuration.VERSION_2_3_30);
                cfg.setNumberFormat("0.##");
                cfg.setClassicCompatible(true);
                StringTemplateLoader stringLoader = new StringTemplateLoader();
                stringLoader.putTemplate("template", content);
                cfg.setTemplateLoader(stringLoader);
                Template temp;
                Writer out = null;
                temp = cfg.getTemplate("template", "utf-8");
                out = new StringWriter();
                temp.process(dataObj, out);
                String resultStr = out.toString();
                out.flush();
                return resultStr;
            }
        } catch (Exception ex) {
            return ex.getMessage();
        }
        return null;
    }
}
