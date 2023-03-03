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

import java.util.List;
import java.util.stream.Collectors;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.notify.core.INotifyPolicyHandler;
import neatlogic.framework.notify.core.NotifyPolicyHandlerFactory;
import neatlogic.framework.notify.dao.mapper.NotifyMapper;
import neatlogic.framework.notify.dto.NotifyPolicyConfigVo;
import neatlogic.framework.notify.dto.NotifyPolicyVo;
import neatlogic.framework.notify.dto.NotifyTriggerVo;
import neatlogic.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import neatlogic.framework.notify.exception.NotifyPolicyNotFoundException;

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
            throw new ParamIrregularException("trigger");
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
