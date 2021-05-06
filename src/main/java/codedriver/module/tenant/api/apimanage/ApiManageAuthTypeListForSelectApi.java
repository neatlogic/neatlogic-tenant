package codedriver.module.tenant.api.apimanage;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.FRAMEWORK_BASE;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.restful.dto.ApiVo;
import codedriver.framework.restful.web.core.ApiAuthFactory;

@Deprecated
@Service
@AuthAction(action = FRAMEWORK_BASE.class)
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
        for ( ApiVo.AuthenticateType s :  ApiVo.AuthenticateType.values()) {
            JSONObject json = new JSONObject();
            json.put("value", s.getValue());
            json.put("text", s.getText());
            if(!s.getValue().equals(ApiVo.AuthenticateType.NOAUTH.getValue())) {
                json.put("help", ApiAuthFactory.getApiAuth(s.getValue()).help());
            }
            resultList.add(json);
        }
        return resultList;
    }

}
