/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.notify;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.dependency.constvalue.CalleeType;
import codedriver.framework.dependency.core.DependencyManager;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import codedriver.framework.auth.label.NOTIFY_POLICY_MODIFY;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.exception.NotifyPolicyReferencedCannotBeDeletedException;

import javax.annotation.Resource;

@Service
@Transactional
@AuthAction(action = NOTIFY_POLICY_MODIFY.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class NotifyPolicyDeleteApi extends PrivateApiComponentBase {

    @Resource
    private NotifyMapper notifyMapper;

    @Override
    public String getToken() {
        return "notify/policy/delete";
    }

    @Override
    public String getName() {
        return "通知策略删除接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "策略id")
    })
    @Output({})
    @Description(desc = "通知策略删除接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        int count = DependencyManager.getDependencyCount(CalleeType.NOTIFY_POLICY, id);
        if (count > 0) {
            throw new NotifyPolicyReferencedCannotBeDeletedException(id.toString());
        }
        notifyMapper.deleteNotifyPolicyById(id);
        return null;
    }

}
