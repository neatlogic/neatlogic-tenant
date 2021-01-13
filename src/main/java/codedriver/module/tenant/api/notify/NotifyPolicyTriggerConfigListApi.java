package codedriver.module.tenant.api.notify;

import java.util.List;
import java.util.stream.Collectors;

import codedriver.framework.restful.core.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.type.ParamIrregularException;
import codedriver.framework.notify.core.INotifyPolicyHandler;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyPolicyConfigVo;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.dto.NotifyTriggerVo;
import codedriver.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyPolicyTriggerConfigListApi extends PrivateApiComponentBase {

    @Autowired
    private NotifyMapper notifyMapper;

    @Override
    public String getToken() {
        return "notify/policy/trigger/config/list";
    }

    @Override
    public String getName() {
        return "通知策略触发动作配置列表接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "policyId", type = ApiParamType.LONG, isRequired = true, desc = "策略id"),
        @Param(name = "trigger", type = ApiParamType.STRING, isRequired = true, desc = "通知触发类型")})
    @Output({@Param(name = "notifyList", type = ApiParamType.JSONARRAY, desc = "通知触发配置列表")})
    @Description(desc = "通知策略触发动作配置列表接口")
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
        List<NotifyTriggerVo> notifyTriggerList = notifyPolicyHandler.getNotifyTriggerList();
        List<Object> notifyTriggerValueList =
            notifyTriggerList.stream().map(NotifyTriggerVo::getTrigger).collect(Collectors.toList());
        String trigger = jsonObj.getString("trigger");
        if (!notifyTriggerValueList.contains(trigger)) {
            throw new ParamIrregularException("参数trigger不符合格式要求");
        }
        JSONObject resultObj = new JSONObject();
        resultObj.put("authorityConfig", notifyPolicyHandler.getAuthorityConfig());
        resultObj.put("notifyList", new JSONArray());
        NotifyPolicyConfigVo config = notifyPolicyVo.getConfig();
        List<NotifyTriggerVo> triggerList = config.getTriggerList();
        for (NotifyTriggerVo triggerObj : triggerList) {
            if (trigger.equals(triggerObj.getTrigger())) {
                resultObj.put("notifyList", triggerObj.getNotifyList());
            }
        }
        return resultObj;
    }

}
