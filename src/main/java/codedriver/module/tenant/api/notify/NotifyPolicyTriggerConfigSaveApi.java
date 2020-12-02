package codedriver.module.tenant.api.notify;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Objects;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.dto.condition.ConditionConfigVo;
import codedriver.framework.exception.type.ParamIrregularException;
import codedriver.framework.notify.core.INotifyHandler;
import codedriver.framework.notify.core.INotifyPolicyHandler;
import codedriver.framework.notify.core.NotifyHandlerFactory;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyActionVo;
import codedriver.framework.notify.dto.NotifyPolicyConfigVo;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.dto.NotifyTemplateVo;
import codedriver.framework.notify.dto.NotifyTriggerNotifyVo;
import codedriver.framework.notify.dto.NotifyTriggerVo;
import codedriver.framework.notify.exception.NotifyHandlerNotFoundException;
import codedriver.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;
import codedriver.framework.notify.exception.NotifyPolicyTriggerConfigNotFoundException;
import codedriver.framework.notify.exception.NotifyTemplateNotFoundException;
import codedriver.framework.notify.exception.NotifyTemplateNotifyHandlerNotMatchException;
import codedriver.framework.util.SnowflakeUtil;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
public class NotifyPolicyTriggerConfigSaveApi extends PrivateApiComponentBase {

    @Autowired
    private NotifyMapper notifyMapper;

    @Override
    public String getToken() {
        return "notify/policy/trigger/config/save";
    }

    @Override
    public String getName() {
        return "通知策略触发动作配置保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "policyId", type = ApiParamType.LONG, isRequired = true, desc = "策略id"),
        @Param(name = "trigger", type = ApiParamType.STRING, isRequired = true, desc = "通知触发类型"),
        @Param(name = "id", type = ApiParamType.LONG, desc = "通知触发配置id"),
        @Param(name = "actionList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "动作列表信息"),
        @Param(name = "conditionConfig", type = ApiParamType.JSONOBJECT, isRequired = true, desc = "条件配置信息")})
    @Output({@Param(name = "id", type = ApiParamType.LONG, desc = "通知触发配置id")})
    @Description(desc = "通知策略触发动作配置保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        Long policyId = jsonObj.getLong("policyId");
        NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
        if (notifyPolicyVo == null) {
            throw new NotifyPolicyNotFoundException(policyId.toString());
        }
        INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyVo.getHandler());
        if (notifyPolicyHandler == null) {
            throw new NotifyPolicyHandlerNotFoundException(notifyPolicyVo.getHandler());
        }
        List<ValueTextVo> notifyTriggerList = notifyPolicyHandler.getNotifyTriggerList();
        String trigger = jsonObj.getString("trigger");
        String triggerName = "";
        for (ValueTextVo notifyTrigger : notifyTriggerList) {
            if (trigger.equals(notifyTrigger.getValue())) {
                triggerName = notifyTrigger.getText();
            }
        }
        if (StringUtils.isBlank(triggerName)) {
            throw new ParamIrregularException("参数trigger不符合格式要求");
        }

        NotifyPolicyConfigVo config = notifyPolicyVo.getConfig();
        JSONArray actionList = jsonObj.getJSONArray("actionList");
        List<NotifyActionVo> actionArray = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(actionList)) {
            List<NotifyTemplateVo> templateList = config.getTemplateList();
            Map<Long, NotifyTemplateVo> templateMap =
                templateList.stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
            for (int index = 0; index < actionList.size(); index++) {
                NotifyActionVo actionObj = actionList.getObject(index, NotifyActionVo.class);
                String notifyHandler = actionObj.getNotifyHandler();
                INotifyHandler handler = NotifyHandlerFactory.getHandler(notifyHandler);
                if (handler == null) {
                    throw new NotifyHandlerNotFoundException(notifyHandler);
                }
                actionObj.setNotifyHandlerName(handler.getName());
                Long templateId = actionObj.getTemplateId();
                NotifyTemplateVo notifyTemplateVo = templateMap.get(templateId);
                if (notifyTemplateVo == null) {
                    throw new NotifyTemplateNotFoundException(templateId.toString());
                }
                if (!Objects.equal(notifyHandler, notifyTemplateVo.getNotifyHandler())) {
                    throw new NotifyTemplateNotifyHandlerNotMatchException(templateId.toString(), handler.getName());
                }
                actionObj.setTemplateName(notifyTemplateVo.getName());
                actionArray.add(actionObj);
            }
        }
        boolean existed = false;
        Long id = jsonObj.getLong("id");
        ConditionConfigVo conditionConfig = jsonObj.getObject("conditionConfig", ConditionConfigVo.class);
        List<NotifyTriggerVo> triggerList = config.getTriggerList();
        for (NotifyTriggerVo triggerObj : triggerList) {
            if (trigger.equals(triggerObj.getTrigger())) {
                existed = true;
                List<NotifyTriggerNotifyVo> notifyList = triggerObj.getNotifyList();
                if (id != null) {
                    boolean isExists = false;
                    for (NotifyTriggerNotifyVo notifyObj : notifyList) {
                        if (id.equals(notifyObj.getId())) {
                            notifyObj.setActionList(actionArray);
                            notifyObj.setConditionConfig(conditionConfig);
                            isExists = true;
                        }
                    }
                    if (!isExists) {
                        throw new NotifyPolicyTriggerConfigNotFoundException(id.toString());
                    }
                    resultObj.put("id", id);
                } else {
                    NotifyTriggerNotifyVo notifyObj = new NotifyTriggerNotifyVo();
                    id = SnowflakeUtil.uniqueLong();
                    notifyObj.setId(id);
                    notifyObj.setActionList(actionArray);
                    notifyObj.setConditionConfig(conditionConfig);
                    notifyList.add(notifyObj);
                    resultObj.put("id", id);
                }
            }
        }
        if (!existed) {
            NotifyTriggerVo triggerObj = new NotifyTriggerVo();
            List<NotifyTriggerNotifyVo> notifyList = new ArrayList<>();
            NotifyTriggerNotifyVo notifyObj = new NotifyTriggerNotifyVo();
            id = SnowflakeUtil.uniqueLong();
            notifyObj.setId(id);
            notifyObj.setActionList(actionArray);
            notifyObj.setConditionConfig(conditionConfig);
            notifyList.add(notifyObj);
            resultObj.put("id", id);
            triggerObj.setNotifyList(notifyList);
            triggerObj.setTrigger(trigger);
            triggerObj.setTriggerName(triggerName);
            triggerList.add(triggerObj);
        }
        notifyMapper.updateNotifyPolicyById(notifyPolicyVo);
        return resultObj;
    }

}
