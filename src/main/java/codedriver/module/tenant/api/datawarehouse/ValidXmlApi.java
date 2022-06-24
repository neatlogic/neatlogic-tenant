/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.datawarehouse;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.DATA_WAREHOUSE_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.datawarehouse.utils.ReportXmlUtil;
import codedriver.framework.dto.DatasourceVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
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