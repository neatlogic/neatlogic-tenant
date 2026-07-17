package neatlogic.module.tenant.service.notify;

import neatlogic.framework.notify.dto.NotifyTriggerVo;

import java.util.Map;

public interface NotifyPolicyService{
    void addReceiverExtraInfo(Map<String, String> moduleUserType, NotifyTriggerVo triggerObj);
}
