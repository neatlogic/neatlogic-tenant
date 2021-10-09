/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.notify;

import java.util.List;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.dto.FieldValidResultVo;
import codedriver.framework.notify.dto.NotifyTriggerVo;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.IValid;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import codedriver.framework.auth.label.NOTIFY_POLICY_MODIFY;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.ConditionParamVo;
import codedriver.framework.notify.core.INotifyPolicyHandler;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyPolicyConfigVo;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import codedriver.framework.notify.exception.NotifyPolicyNameRepeatException;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;

@Service
@Transactional
@AuthAction(action = NOTIFY_POLICY_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class NotifyPolicySaveApi extends PrivateApiComponentBase {

    @Autowired
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
        @Param(name = "name", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]{1,50}$",
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
        if (id != null) {
            NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(id);
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
            return notifyPolicyVo;
        } else {
            NotifyPolicyVo notifyPolicyVo = new NotifyPolicyVo(name, handler);
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
            return notifyPolicyVo;
        }
    }

    public IValid name(){
        return value -> {
            NotifyPolicyVo notifyPolicyVo = JSON.toJavaObject(value,NotifyPolicyVo.class);
            if (notifyMapper.checkNotifyPolicyNameIsRepeat(notifyPolicyVo) > 0) {
                return new FieldValidResultVo(new NotifyPolicyNameRepeatException(notifyPolicyVo.getName()));
            }
            return new FieldValidResultVo();
        };
    }

}
