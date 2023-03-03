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
