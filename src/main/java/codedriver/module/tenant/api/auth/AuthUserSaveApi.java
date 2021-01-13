package codedriver.module.tenant.api.auth;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserAuthVo;
import codedriver.framework.restful.core.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import codedriver.framework.auth.label.AUTHORITY_MODIFY;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-19 18:09
 **/
@Service
@AuthAction(action = AUTHORITY_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class AuthUserSaveApi extends PrivateApiComponentBase {


    @Autowired
    private UserMapper userMapper;

    @Override
    public String getToken() {
        return "auth/user/save";
    }

    @Override
    public String getName() {
        return "权限用户保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param( name = "auth", desc = "权限", type = ApiParamType.STRING, isRequired = true),
            @Param( name = "authGroup", desc = "权限组", type = ApiParamType.STRING, isRequired = true),
            @Param( name = "userUuidList", desc = "用户uuid集合", type = ApiParamType.JSONARRAY)
    })
    @Description(desc = "权限用户保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<String> userUuidList = JSON.parseArray(jsonObj.getString("userUuidList"), String.class);
        if(CollectionUtils.isNotEmpty(userUuidList)) {
        	String authGroup = jsonObj.getString("authGroup");
        	String auth = jsonObj.getString("auth");
            userMapper.deleteUserAuth(new UserAuthVo(null, auth));
            List<String> existUserUuidList = userMapper.checkUserUuidListIsExists(userUuidList);
			userUuidList.retainAll(existUserUuidList);
        	for(String userUuid : userUuidList) {
        		UserAuthVo userAuthVo = new UserAuthVo();
                userAuthVo.setAuthGroup(authGroup);
                userAuthVo.setAuth(auth);
                userAuthVo.setUserUuid(userUuid);
                userMapper.insertUserAuth(userAuthVo);
            }
        }

        return null;
    }
}
