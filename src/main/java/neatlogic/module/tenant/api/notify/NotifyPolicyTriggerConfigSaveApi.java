/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tenant.api.notify;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import neatlogic.framework.auth.label.NOTIFY_POLICY_MODIFY;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Objects;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.condition.ConditionConfigVo;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.notify.core.INotifyHandler;
import neatlogic.framework.notify.core.INotifyPolicyHandler;
import neatlogic.framework.notify.core.NotifyHandlerFactory;
import neatlogic.framework.notify.core.NotifyPolicyHandlerFactory;
import neatlogic.framework.notify.dao.mapper.NotifyMapper;
import neatlogic.framework.notify.dto.NotifyActionVo;
import neatlogic.framework.notify.dto.NotifyPolicyConfigVo;
import neatlogic.framework.notify.dto.NotifyPolicyVo;
import neatlogic.framework.notify.dto.NotifyTemplateVo;
import neatlogic.framework.notify.dto.NotifyTriggerNotifyVo;
import neatlogic.framework.notify.dto.NotifyTriggerVo;
import neatlogic.framework.notify.exception.NotifyHandlerNotFoundException;
import neatlogic.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import neatlogic.framework.notify.exception.NotifyPolicyNotFoundException;
import neatlogic.framework.notify.exception.NotifyPolicyTriggerConfigNotFoundException;
import neatlogic.framework.notify.exception.NotifyTemplateNotFoundException;
import neatlogic.framework.notify.exception.NotifyTemplateNotifyHandlerNotMatchException;
import neatlogic.framework.util.SnowflakeUtil;

@Service
@Transactional
@AuthAction(action = NOTIFY_POLICY_MODIFY.class)
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
        List<NotifyTriggerVo> notifyTriggerVoList = notifyPolicyHandler.getNotifyTriggerList();
        List<String> notifyTriggerList = new ArrayList<>(notifyTriggerVoList.size());
        String trigger = jsonObj.getString("trigger");
        String triggerName = "";
        for (NotifyTriggerVo notifyTrigger : notifyTriggerVoList) {
            notifyTriggerList.add(notifyTrigger.getTrigger());
            if (trigger.equals(notifyTrigger.getTrigger())) {
                triggerName = notifyTrigger.getTriggerName();
            }
        }
        if (StringUtils.isBlank(triggerName)) {
            throw new ParamIrregularException("trigger");
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
        Iterator<NotifyTriggerVo> iterator = triggerList.iterator();
        while(iterator.hasNext()){
            NotifyTriggerVo triggerObj = iterator.next();
            if(notifyTriggerList.contains(triggerObj.getTrigger())){
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
            }else{
                iterator.remove();
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
