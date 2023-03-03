package neatlogic.module.tenant.api.auth;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserAuthVo;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import neatlogic.framework.auth.label.AUTHORITY_MODIFY;
import neatlogic.framework.service.UserService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * @program: neatlogic
 * @description:
 * @create: 2020-03-19 18:09
 **/
@Service
@AuthAction(action = AUTHORITY_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
@Transactional
public class AuthUserSaveApi extends PrivateApiComponentBase {


    @Resource
    private UserMapper userMapper;

    @Resource
    private UserService userService;

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
            @Param( name = "userUuidList", desc = "用户uuid集合", type = ApiParamType.JSONARRAY),
            @Param( name = "teamUuidList", desc = "分组uuid集合", type = ApiParamType.JSONARRAY)
    })
    @Description(desc = "权限用户保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String authGroup = jsonObj.getString("authGroup");
        String auth = jsonObj.getString("auth");
        List<String> userUuidList = JSON.parseArray(jsonObj.getString("userUuidList"), String.class);
        List<String> teamUuidList = JSON.parseArray(jsonObj.getString("teamUuidList"), String.class);
        Set<String> uuidList = userService.getUserUuidSetByUserUuidListAndTeamUuidList(userUuidList,teamUuidList);

        if(CollectionUtils.isNotEmpty(uuidList)){
            for(String userUuid : uuidList) {
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
