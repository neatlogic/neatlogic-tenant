/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.user;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional

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
