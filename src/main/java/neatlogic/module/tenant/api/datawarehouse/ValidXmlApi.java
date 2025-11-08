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

package neatlogic.module.tenant.api.datawarehouse;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.DATA_WAREHOUSE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.datawarehouse.utils.ReportXmlUtil;
import neatlogic.framework.dto.DatasourceVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
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
