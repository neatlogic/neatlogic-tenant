package codedriver.module.tenant.api.notify;

import java.util.List;
import java.util.Objects;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.ConditionParamVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.notify.core.INotifyPolicyHandler;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyPolicyGetApi  extends PrivateApiComponentBase {
	
	@Autowired
	private NotifyMapper notifyMapper;
	
	@Autowired
	private UserMapper userMapper;

	@Override
	public String getToken() {
		return "notify/policy/get";
	}

	@Override
	public String getName() {
		return "通知策略信息获取接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "策略id")
	})
	@Output({
		@Param(explode = NotifyPolicyVo.class, desc = "策略信息")
	})
	@Description(desc = "通知策略信息获取接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long id = jsonObj.getLong("id");
		NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(id);
		if(notifyPolicyVo == null) {
			throw new NotifyPolicyNotFoundException(id.toString());
		}
		INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyVo.getHandler());
		if(notifyPolicyHandler == null) {
			throw new NotifyPolicyHandlerNotFoundException(notifyPolicyVo.getHandler());
		}
        JSONObject config = notifyPolicyVo.getConfig();
        JSONArray triggerList = config.getJSONArray("triggerList");
        List<ValueTextVo> notifyTriggerList = notifyPolicyHandler.getNotifyTriggerList();
        JSONArray triggerArray = new JSONArray();
        for (ValueTextVo notifyTrigger : notifyTriggerList) {
            boolean existed = false;
            for(int i = 0; i < triggerList.size(); i++) {
                JSONObject triggerObj = triggerList.getJSONObject(i);
                if(Objects.equals(notifyTrigger.getValue(), triggerObj.getString("trigger"))) {
                    triggerArray.add(triggerObj);
                    existed = true;
                    break;
                }
            }
            if(!existed) {
                JSONObject triggerObj = new JSONObject();
                triggerObj.put("trigger", notifyTrigger.getValue());
                triggerObj.put("triggerName", notifyTrigger.getText());
                triggerObj.put("notifyList", new JSONArray());
                triggerArray.add(triggerObj);
            }
        }
        config.put("triggerList", triggerArray);		
        List<ConditionParamVo> systemParamList = notifyPolicyHandler.getSystemParamList();
        List<ConditionParamVo> systemConditionOptionList = notifyPolicyHandler.getSystemConditionOptionList();
        JSONArray paramList = config.getJSONArray("paramList");
        if(CollectionUtils.isNotEmpty(paramList)) {
            for(int i = 0; i < paramList.size(); i++) {
                ConditionParamVo param = paramList.getObject(i, ConditionParamVo.class);
                systemParamList.add(param);
                systemConditionOptionList.add(new ConditionParamVo(param));
            }
        }
        
        systemParamList.sort((e1, e2) -> e1.getName().compareToIgnoreCase(e2.getName()));
        systemConditionOptionList.sort((e1, e2) -> e1.getName().compareToIgnoreCase(e2.getName()));
		config.put("paramList", systemParamList);
		config.put("conditionOptionList", systemConditionOptionList);
		List<String> adminUserUuidList = JSON.parseArray(JSON.toJSONString(config.getJSONArray("adminUserUuidList")), String.class);
		if(CollectionUtils.isNotEmpty(adminUserUuidList)) {
			List<UserVo> userList = userMapper.getUserByUserUuidList(adminUserUuidList);
			config.put("userList", userList);
		}else {
			config.put("userList", new JSONArray());
		}
		notifyPolicyVo.setConfig(config.toJSONString());
		return notifyPolicyVo;
	}

}
