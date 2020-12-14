package codedriver.module.tenant.service.notify;

import codedriver.framework.notify.dto.NotifyTriggerVo;

import java.util.Map;

public interface NotifyPolicyService{
    public void addReceiverExtraInfo(Map<String, String> processUserType, NotifyTriggerVo triggerObj);
}
