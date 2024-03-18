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
@Deprecated
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
