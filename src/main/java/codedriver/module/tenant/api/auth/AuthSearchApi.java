package codedriver.module.tenant.api.auth;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.auth.core.AuthBase;
import codedriver.framework.auth.core.AuthFactory;
import codedriver.framework.auth.core.AuthGroupEnum;
import codedriver.framework.dto.AuthGroupVo;
import codedriver.framework.dto.AuthVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AuthSearchApi extends ApiComponentBase {

    @Override
    public String getToken() {
        return "auth/search";
    }

    @Override
    public String getName() {
        return "权限列表查询接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({
            @Param( name = "authGroupList", type = ApiParamType.JSONARRAY, desc = "权限列表组集合", explode = AuthGroupVo[].class)
    })
    @Description(desc = "权限列表查询接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        List<AuthGroupVo> authGroupVoList = new ArrayList<>();
        Map<String, List<AuthBase>> authGroupMap = AuthFactory.getAuthGroupMap();
        for (Map.Entry<String, List<AuthBase>> entry : authGroupMap.entrySet()){
            String authGroupName = entry.getKey();
            AuthGroupVo authGroupVo = new AuthGroupVo();
            authGroupVo.setName(authGroupName);
            authGroupVo.setDisplayName(AuthGroupEnum.getText(authGroupName));
            List<AuthBase> authList = authGroupMap.get(authGroupName);
            if (authList != null && authList.size() > 0){
                List<AuthVo> authArray = new ArrayList<>();
                for (AuthBase authBase : authList){
                    AuthVo authVo = new AuthVo();
                    authVo.setName(authBase.getAuthName());
                    authVo.setDisplayName(authBase.getAuthDisplayName());
                    authArray.add(authVo);
                }
                authGroupVo.setAuthVoList(authArray);
            }
            authGroupVoList.add(authGroupVo);
        }
        returnObj.put("authGroupList", authGroupVoList);
        return returnObj;
    }
}
