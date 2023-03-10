/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        return "??????????????????????????????????????????";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "policyId", type = ApiParamType.LONG, isRequired = true, desc = "??????id"),
        @Param(name = "trigger", type = ApiParamType.STRING, isRequired = true, desc = "??????????????????"),
        @Param(name = "id", type = ApiParamType.LONG, desc = "??????????????????id"),
        @Param(name = "actionList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "??????????????????"),
        @Param(name = "conditionConfig", type = ApiParamType.JSONOBJECT, isRequired = true, desc = "??????????????????")})
    @Output({@Param(name = "id", type = ApiParamType.LONG, desc = "??????????????????id")})
    @Description(desc = "??????????????????????????????????????????")
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
