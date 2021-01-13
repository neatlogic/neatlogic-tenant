package codedriver.module.tenant.api.user;

import codedriver.framework.auth.core.AuthAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.label.NO_AUTH;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.restful.core.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

@Service
@Transactional
@AuthAction(action = NO_AUTH.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class CurrentUserSaveApi extends PrivateApiComponentBase {

    @Autowired
    UserMapper userMapper;

    @Autowired
    TeamMapper teamMapper;

    @Override
    public String getToken() {
        return "user/current/save";
    }

    @Override
    public String getName() {
        return "保存当前用户接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "userName", type = ApiParamType.STRING, desc = "用户姓名", isRequired = true, xss = true),
        @Param(name = "email", type = ApiParamType.STRING, desc = "用户邮箱", isRequired = false, xss = true),
        @Param(name = "phone", type = ApiParamType.STRING, desc = "用户电话", isRequired = false, xss = true),
        @Param(name = "userInfo", type = ApiParamType.STRING, desc = "其他信息", isRequired = false, xss = true)})
    @Output({})
    @Description(desc = "保存用户接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        UserVo currentUserVo = userMapper.getUserBaseInfoByUuid(UserContext.get().getUserUuid(true));
        currentUserVo.setUserName(jsonObj.getString("userName"));
        currentUserVo.setEmail(jsonObj.getString("email"));
        currentUserVo.setPhone(jsonObj.getString("phone"));
        currentUserVo.setUserInfo(jsonObj.getString("userInfo"));
        userMapper.updateUser(currentUserVo);
        return currentUserVo.getUuid();
    }
}
