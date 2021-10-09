/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.notify;

import java.util.List;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.auth.label.NOTIFY_POLICY_MODIFY;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.exception.user.UserNotFoundException;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyPolicyConfigVo;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

@Service
@Transactional
@AuthAction(action = NOTIFY_POLICY_MODIFY.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class NotifyPolicyExceptionNotifyDeleteApi extends PrivateApiComponentBase {

    @Autowired
    private NotifyMapper notifyMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public String getToken() {
        return "notify/policy/exceptionnotify/delete";
    }

    @Override
    public String getName() {
        return "通知策略异常通知删除接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "policyId", type = ApiParamType.LONG, isRequired = true, desc = "策略id"),
        @Param(name = "userUuid", type = ApiParamType.STRING, isRequired = true, desc = "用户uuid")})
    @Description(desc = "通知策略管理员删除接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long policyId = jsonObj.getLong("policyId");
        NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
        if (notifyPolicyVo == null) {
            throw new NotifyPolicyNotFoundException(policyId.toString());
        }
        String userUuid = jsonObj.getString("userUuid");
        if (userMapper.checkUserIsExists(userUuid) == 0) {
            throw new UserNotFoundException(userUuid);
        }
        NotifyPolicyConfigVo config = notifyPolicyVo.getConfig();
        List<String> adminUserUuidList = config.getAdminUserUuidList();
        adminUserUuidList.remove(userUuid);
        notifyMapper.updateNotifyPolicyById(notifyPolicyVo);
        return null;
    }

}
