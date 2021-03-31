package codedriver.module.tenant.api.user;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.cache.UserSessionCache;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserAuthVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.user.UserNotFoundException;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = AUTHORITY_MODIFY.class)
public class UserActiveUpdateApi extends PrivateApiComponentBase {

    @Autowired
    private UserMapper userMapper;

    @Override
    public String getToken() {
        return "user/active";
    }

    @Override
    public String getName() {
        return "用户有效性变更接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "userUuidList", type = ApiParamType.JSONARRAY, desc = "用户uuid集合", isRequired = true),
            @Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", desc = "有效性", isRequired = true)
    })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Integer isActive = jsonObj.getInteger("isActive");
        List<String> userUuidList = JSON.parseArray(jsonObj.getString("userUuidList"), String.class);
        if (CollectionUtils.isNotEmpty(userUuidList)) {
            UserVo userVo = new UserVo();
            userVo.setIsActive(isActive);
            String tenantUuid = TenantContext.get().getTenantUuid();
            for (String userUuid : userUuidList) {
                if (userMapper.checkUserIsExists(userUuid) == 0) {
                    throw new UserNotFoundException(userUuid);
                }
                userVo.setUuid(userUuid);
                userMapper.updateUserActive(userVo);
                //禁用用户时删除userSession
                UserSessionCache.removeItem(tenantUuid, userUuid);
                userMapper.deleteUserSessionByUserUuid(userUuid);
//                if(isActive == 0){
//                    userMapper.deleteUserAuth(new UserAuthVo(userUuid));
//                    userMapper.deleteUserRoleByUserUuid(userUuid);
//                    userMapper.deleteUserTeamByUserUuid(userUuid);
//                }
            }
        }
        return null;
    }
}
