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

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NOTIFY_POLICY_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.ConditionParamVo;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.notify.core.INotifyPolicyHandler;
import neatlogic.framework.notify.core.NotifyPolicyHandlerFactory;
import neatlogic.framework.notify.dao.mapper.NotifyMapper;
import neatlogic.framework.notify.dto.NotifyPolicyConfigVo;
import neatlogic.framework.notify.dto.NotifyPolicyVo;
import neatlogic.framework.notify.dto.NotifyTriggerVo;
import neatlogic.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import neatlogic.framework.notify.exception.NotifyPolicyMoreThanOneException;
import neatlogic.framework.notify.exception.NotifyPolicyNameRepeatException;
import neatlogic.framework.notify.exception.NotifyPolicyNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.RegexUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
@AuthAction(action = NOTIFY_POLICY_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class NotifyPolicySaveApi extends PrivateApiComponentBase {

    @Resource
    private NotifyMapper notifyMapper;

    @Override
    public String getToken() {
        return "notify/policy/save";
    }

    @Override
    public String getName() {
        return "通知策略信息保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "策略id"),
            @Param(name = "name", type = ApiParamType.REGEX, rule = RegexUtils.NAME, maxLength = 50,
                    isRequired = true, desc = "策略名"),
            @Param(name = "handler", type = ApiParamType.STRING, isRequired = true, desc = "通知策略处理器")})
    @Output({@Param(explode = NotifyPolicyVo.class, desc = "策略信息")})
    @Description(desc = "通知策略信息保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String handler = jsonObj.getString("handler");
        INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(handler);
        if (notifyPolicyHandler == null) {
            throw new NotifyPolicyHandlerNotFoundException(handler);
        }
        Long id = jsonObj.getLong("id");
        String name = jsonObj.getString("name");
        NotifyPolicyVo notifyPolicyVo;
        if (id != null) {
            notifyPolicyVo = notifyMapper.getNotifyPolicyById(id);
            if (notifyPolicyVo == null) {
                throw new NotifyPolicyNotFoundException(id.toString());
            }
            notifyPolicyVo.setName(name);
            if (notifyMapper.checkNotifyPolicyNameIsRepeat(notifyPolicyVo) > 0) {
                throw new NotifyPolicyNameRepeatException(name);
            }
            notifyPolicyVo.setLcu(UserContext.get().getUserUuid(true));
            notifyMapper.updateNotifyPolicyById(notifyPolicyVo);
            NotifyPolicyConfigVo config = notifyPolicyVo.getConfig();
            List<ConditionParamVo> paramList = config.getParamList();
            paramList.addAll(notifyPolicyHandler.getSystemParamList());
            paramList.sort((e1, e2) -> e1.getName().compareToIgnoreCase(e2.getName()));
        } else {
            if (notifyMapper.getNotifyPolicyByHandlerLimitOne(handler) != null && notifyPolicyHandler.isAllowMultiPolicy() == 0) {
                throw new NotifyPolicyMoreThanOneException(notifyPolicyHandler.getName());
            }
            notifyPolicyVo = new NotifyPolicyVo(name, handler);
            if (notifyMapper.checkNotifyPolicyNameIsRepeat(notifyPolicyVo) > 0) {
                throw new NotifyPolicyNameRepeatException(name);
            }
            notifyPolicyVo.setFcu(UserContext.get().getUserUuid(true));
            JSONObject configObj = new JSONObject();
            JSONArray triggerList = new JSONArray();
            for (NotifyTriggerVo notifyTrigger : notifyPolicyHandler.getNotifyTriggerList()) {
                JSONObject triggerObj = new JSONObject();
                triggerObj.put("trigger", notifyTrigger.getTrigger());
                triggerObj.put("triggerName", notifyTrigger.getTriggerName());
                triggerObj.put("description", notifyTrigger.getDescription());
                triggerObj.put("notifyList", new JSONArray());
                triggerList.add(triggerObj);
            }
            configObj.put("triggerList", triggerList);
            configObj.put("paramList", new JSONArray());
            configObj.put("templateList", new JSONArray());
            configObj.put("adminUserUuidList", new JSONArray());
            notifyPolicyVo.setConfig(configObj.toJSONString());
            notifyMapper.insertNotifyPolicy(notifyPolicyVo);
            List<ConditionParamVo> paramList = notifyPolicyHandler.getSystemParamList();
            paramList.sort((e1, e2) -> e1.getName().compareToIgnoreCase(e2.getName()));
            configObj.put("paramList", paramList);
            notifyPolicyVo.setConfig(configObj.toJSONString());
        }
        return notifyPolicyVo;
    }

    public IValid name() {
        return value -> {
            NotifyPolicyVo notifyPolicyVo = JSON.toJavaObject(value, NotifyPolicyVo.class);
            if (notifyMapper.checkNotifyPolicyNameIsRepeat(notifyPolicyVo) > 0) {
                return new FieldValidResultVo(new NotifyPolicyNameRepeatException(notifyPolicyVo.getName()));
            }
            return new FieldValidResultVo();
        };
    }

}
