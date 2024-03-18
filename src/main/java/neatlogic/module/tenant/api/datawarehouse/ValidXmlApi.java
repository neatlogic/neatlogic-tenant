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

package neatlogic.module.tenant.api.datawarehouse;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.DATA_WAREHOUSE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.datawarehouse.utils.ReportXmlUtil;
import neatlogic.framework.dto.DatasourceVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = DATA_WAREHOUSE_MODIFY.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class ValidXmlApi extends PrivateApiComponentBase {


    @Override
    public String getToken() {
        return "datawarehouse/datasource/validxml";
    }

    @Override
    public String getName() {
        return "验证数据源XML";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "xml", type = ApiParamType.STRING, desc = "xml内容")})
    @Output({@Param(explode = DatasourceVo.class)})
    @Description(desc = "验证数据源XML，如果验证通过则返回字段等信息")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String xml = jsonObj.getString("xml");
        try {
            return ReportXmlUtil.generateDataSourceFromXml(xml);
        } catch (Exception ex) {
            JSONObject obj = new JSONObject();
            obj.put("error", "语法错误，异常：" + ex.getMessage());
            return obj;
        }
    }

}
