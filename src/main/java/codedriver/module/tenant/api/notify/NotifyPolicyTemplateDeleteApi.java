package codedriver.module.tenant.api.notify;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.module.tenant.auth.label.NOTIFY_POLICY_MODIFY;
import org.omg.PortableServer.ForwardRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.notify.core.INotifyPolicyHandler;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyActionVo;
import codedriver.framework.notify.dto.NotifyPolicyConfigVo;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.dto.NotifyTemplateVo;
import codedriver.framework.notify.dto.NotifyTriggerNotifyVo;
import codedriver.framework.notify.dto.NotifyTriggerVo;
import codedriver.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;
import codedriver.framework.notify.exception.NotifyTemplateReferencedCannotBeDeletedException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

@Service
@Transactional
@AuthAction(action = NOTIFY_POLICY_MODIFY.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class NotifyPolicyTemplateDeleteApi extends PrivateApiComponentBase {

    @Autowired
    private NotifyMapper notifyMapper;

    @Override
    public String getToken() {
        return "notify/policy/template/delete";
    }

    @Override
    public String getName() {
        return "通知模板删除接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "policyId", type = ApiParamType.LONG, isRequired = true, desc = "策略id"),
            @Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "模板id")})
    @Description(desc = "通知模板删除接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long policyId = jsonObj.getLong("policyId");
        NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
        if (notifyPolicyVo == null) {
            throw new NotifyPolicyNotFoundException(policyId.toString());
        }
        INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyVo.getHandler());
        if (notifyPolicyHandler == null) {
            throw new NotifyPolicyHandlerNotFoundException(notifyPolicyVo.getHandler());
        }

        Long id = jsonObj.getLong("id");
        int index = -1;
        String notifyTemplateName = null;
        NotifyPolicyConfigVo config = notifyPolicyVo.getConfig();
        List<NotifyTemplateVo> templateList = config.getTemplateList();
        for (int i = 0; i < templateList.size(); i++) {
            NotifyTemplateVo notifyTemplateVo = templateList.get(i);
            if (id.equals(notifyTemplateVo.getId())) {
                index = i;
                notifyTemplateName = notifyTemplateVo.getName();
            }
        }
        if (index == -1) {
            return null;
        }
        // 判断模板是否被引用
        List<NotifyTriggerVo> triggerList = config.getTriggerList();
        for (NotifyTriggerVo notifyTriggerVo : triggerList) {
            for (NotifyTriggerNotifyVo notifyTriggerNotifyVo : notifyTriggerVo.getNotifyList()) {
                for (NotifyActionVo notifyActionVo : notifyTriggerNotifyVo.getActionList()) {
                    if (Objects.equals(notifyActionVo.getTemplateId(), id)) {
                        throw new NotifyTemplateReferencedCannotBeDeletedException(notifyTemplateName);
                    }
                }
            }
        }
        templateList.remove(index);
        notifyMapper.updateNotifyPolicyById(notifyPolicyVo);
        return null;
    }

}
