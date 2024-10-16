package neatlogic.module.tenant.api.user;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.AUTHORITY_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.exception.user.UserNotFoundException;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.service.UserSessionService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = AUTHORITY_MODIFY.class)
public class UserActiveUpdateApi extends PrivateApiComponentBase {

    @Resource
    private UserMapper userMapper;
    @Resource
    UserSessionService userSessionService;

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
//                if(isActive == 0){
//                    userMapper.deleteUserAuth(new UserAuthVo(userUuid));
//                    userMapper.deleteUserRoleByUserUuid(userUuid);
//                    userMapper.deleteUserTeamByUserUuid(userUuid);
//                }
            }
            //userSessionService.deleteUserSessionByUserUuid(userUuidList);
        }
        return null;
    }
}
