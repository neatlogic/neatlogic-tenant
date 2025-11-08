/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.api.notify;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
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
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@AuthAction(action = NoAuth.class)
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
        String trigger = jsonObj.getString("trigger");
        NotifyTriggerVo notifyTriggerVo = null;
        for (NotifyTriggerVo triggerVo : notifyTriggerList) {
            if (Objects.equals(triggerVo.getTrigger(), trigger)) {
                notifyTriggerVo = triggerVo;
                break;
            }
        }
        if (notifyTriggerVo == null) {
            throw new ParamIrregularException("trigger");
        }
        JSONObject resultObj = new JSONObject();
        JSONObject authorityConfig = notifyPolicyHandler.getAuthorityConfig();
        JSONArray includeArray = authorityConfig.getJSONArray("includeList");
        JSONArray excludeArray = authorityConfig.getJSONArray("excludeList");
        List<String> includeList = notifyTriggerVo.getIncludeList();
        if (CollectionUtils.isNotEmpty(includeList)) {
            for (String include : includeList) {
                if (!includeArray.contains(include)) {
                    includeArray.add(include);
                }
                excludeArray.remove(include);
            }
        }
        List<String> excludeList = notifyTriggerVo.getExcludeList();
        if (CollectionUtils.isNotEmpty(excludeList)) {
            for (String exclude : excludeList) {
                if (!excludeArray.contains(exclude)) {
                    excludeArray.add(exclude);
                }
                includeArray.remove(exclude);
            }
        }
        resultObj.put("authorityConfig", authorityConfig);
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
