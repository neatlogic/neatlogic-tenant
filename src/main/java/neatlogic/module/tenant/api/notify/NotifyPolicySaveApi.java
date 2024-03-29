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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import neatlogic.framework.util.$;
import neatlogic.framework.util.RegexUtils;
import org.apache.commons.collections4.CollectionUtils;
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
        return "nmtan.notifypolicysaveapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "common.id"),
            @Param(name = "name", type = ApiParamType.REGEX, rule = RegexUtils.NAME, maxLength = 50, isRequired = true, desc = "common.name"),
            @Param(name = "handler", type = ApiParamType.STRING, isRequired = true, desc = "term.notify.handler")
    })
    @Output({
            @Param(explode = NotifyPolicyVo.class, desc = "common.tbodylist")
    })
    @Description(desc = "nmtan.notifypolicysaveapi.getname")
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
                throw new NotifyPolicyMoreThanOneException($.t(notifyPolicyHandler.getName()));
            }
            notifyPolicyVo = new NotifyPolicyVo(name, handler);
            if (notifyMapper.checkNotifyPolicyNameIsRepeat(notifyPolicyVo) > 0) {
                throw new NotifyPolicyNameRepeatException(name);
            }
            notifyPolicyVo.setFcu(UserContext.get().getUserUuid(true));
            JSONObject configObj = new JSONObject();
            JSONArray triggerList = new JSONArray();
            if (CollectionUtils.isNotEmpty(notifyPolicyHandler.getNotifyTriggerList())) {
                for (NotifyTriggerVo notifyTrigger : notifyPolicyHandler.getNotifyTriggerList()) {
                    JSONObject triggerObj = new JSONObject();
                    triggerObj.put("trigger", notifyTrigger.getTrigger());
                    triggerObj.put("triggerName", notifyTrigger.getTriggerName());
                    triggerObj.put("description", notifyTrigger.getDescription());
                    triggerObj.put("notifyList", new JSONArray());
                    triggerList.add(triggerObj);
                }
            }
            configObj.put("triggerList", triggerList);
            configObj.put("paramList", new JSONArray());
            configObj.put("templateList", new JSONArray());
            configObj.put("adminUserUuidList", new JSONArray());
            notifyPolicyVo.setConfig(configObj.toJSONString());
            notifyPolicyVo.setIsDefault(0);
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
