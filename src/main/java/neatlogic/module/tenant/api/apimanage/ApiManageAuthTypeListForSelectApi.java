/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.tenant.api.apimanage;

import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.auth.core.ApiAuthFactory;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.restful.enums.PublicApiAuthType;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

@Deprecated
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ApiManageAuthTypeListForSelectApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "apimanage/authtype/list/forselect";
    }

    @Override
    public String getName() {
        return "获取接口组件认证方式列表_下拉框";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({})
    @Output({@Param(explode = ValueTextVo[].class, desc = "组件认证方式列表")})
    @Description(desc = "获取接口组件认证方式列表_下拉框")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray resultList = new JSONArray();
        for (PublicApiAuthType s : PublicApiAuthType.values()) {
            JSONObject json = new JSONObject();
            json.put("value", s.getValue());
            json.put("text", s.getText());
            json.put("help", ApiAuthFactory.getApiAuth(s.getValue()).help());
            resultList.add(json);
        }
        return resultList;
    }

}
