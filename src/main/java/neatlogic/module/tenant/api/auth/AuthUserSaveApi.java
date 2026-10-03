package neatlogic.module.tenant.api.auth;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.core.AuthFactory;
import neatlogic.framework.auth.label.AUTHORITY_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.systemuser.ISystemUser;
import neatlogic.framework.common.constvalue.systemuser.SystemUserFactory;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserAuthVo;
import neatlogic.framework.exception.auth.AuthNotFoundException;
import neatlogic.framework.exception.user.UserNotFoundException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.service.UserService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 为普通用户或系统内置用户追加指定权限，普通用户仍支持通过分组展开授权。
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

    /** 系统分支仅处理精确注册 UUID，整批校验通过后追加权限；普通分支沿用用户与分组展开逻辑。 */
    @Input({
            @Param( name = "auth", desc = "权限", type = ApiParamType.STRING, isRequired = true),
            @Param( name = "authGroup", desc = "权限组", type = ApiParamType.STRING, isRequired = true),
            @Param( name = "userUuidList", desc = "用户uuid集合", type = ApiParamType.JSONARRAY),
            @Param( name = "userType", desc = "用户类型，默认普通用户", type = ApiParamType.STRING, rule = "user,system"),
            @Param( name = "teamUuidList", desc = "分组uuid集合", type = ApiParamType.JSONARRAY)
    })
    @Description(desc = "权限用户保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String authGroup = jsonObj.getString("authGroup");
        String auth = jsonObj.getString("auth");
        List<String> userUuidList = JSON.parseArray(jsonObj.getString("userUuidList"), String.class);
        List<String> teamUuidList = JSON.parseArray(jsonObj.getString("teamUuidList"), String.class);
        Set<String> uuidList;
        if ("system".equalsIgnoreCase(jsonObj.getString("userType"))) {
            if (AuthFactory.getAuthInstance(auth) == null) {
                throw new AuthNotFoundException(auth);
            }
            Set<String> registeredUuidSet = new HashSet<>();
            for (ISystemUser systemUser : SystemUserFactory.getSystemUserList()) {
                registeredUuidSet.add(systemUser.getUserUuid());
            }
            uuidList = new HashSet<>();
            if (CollectionUtils.isNotEmpty(userUuidList)) {
                // 先校验完整批次，防止未知 UUID 导致部分目标已被写入。
                for (String userUuid : userUuidList) {
                    if (!registeredUuidSet.contains(userUuid)) {
                        throw new UserNotFoundException(userUuid);
                    }
                    uuidList.add(userUuid);
                }
            }
        } else {
            uuidList = userService.getUserUuidSetByUserUuidListAndTeamUuidList(userUuidList,teamUuidList);
        }

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
