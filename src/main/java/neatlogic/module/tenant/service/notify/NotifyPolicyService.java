package neatlogic.module.tenant.service.notify;

import neatlogic.framework.notify.dto.NotifyTriggerVo;

import java.util.Map;

public interface NotifyPolicyService{
    public void addReceiverExtraInfo(Map<String, String> processUserType, NotifyTriggerVo triggerObj);
}
