package codedriver.module.tenant.api.notify;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.notify.core.INotifyPolicyHandler;
import codedriver.framework.notify.core.NotifyPolicyFactory;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import codedriver.framework.notify.exception.NotifyPolicyNameRepeatException;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@Transactional
public class NotifyPolicySaveApi  extends ApiComponentBase {
	
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

	@Input({
		@Param(name = "id", type = ApiParamType.LONG, desc = "策略id"),
		@Param(name = "name", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]{1,50}$", isRequired = true, desc = "策略名"),
		@Param(name = "handler", type = ApiParamType.STRING, isRequired = true, desc = "通知策略处理器")
	})
	@Output({
		@Param(name = "notifyPolicy", explode = NotifyPolicyVo.class, desc = "策略信息")
	})
	@Description(desc = "通知策略信息保存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String handler = jsonObj.getString("handler");
		INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(handler);
		if(notifyPolicyHandler == null) {
			throw new NotifyPolicyHandlerNotFoundException(handler);
		}
		Long id = jsonObj.getLong("id");
		String name = jsonObj.getString("name");
		if(id != null) {
			NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(id);
			if(notifyPolicyVo == null) {
				throw new NotifyPolicyNotFoundException(id.toString());
			}
			notifyPolicyVo.setName(name);
			if(notifyMapper.checkNotifyPolicyNameIsRepeat(notifyPolicyVo) > 0) {
				throw new NotifyPolicyNameRepeatException(name);
			}
			notifyMapper.updateNotifyPolicyById(notifyPolicyVo);
			return notifyPolicyVo;
		}else {
			NotifyPolicyVo notifyPolicyVo = new NotifyPolicyVo(name, handler);
			notifyPolicyVo.setFcu(UserContext.get().getUserUuid(true));
			JSONObject configObj = new JSONObject();
			JSONArray triggerList = new JSONArray();
			for (ValueTextVo notifyTrigger : notifyPolicyHandler.getNotifyTriggerList()) {
				JSONObject triggerObj = new JSONObject();
				triggerObj.put("trigger", notifyTrigger.getValue());
				triggerObj.put("triggerName", notifyTrigger.getText());
				triggerObj.put("notifyList", new JSONArray());
				triggerList.add(triggerObj);
			}
			configObj.put("triggerList", triggerList);
			configObj.put("paramList", new JSONArray());
			configObj.put("templateList", new JSONArray());
			notifyPolicyVo.setConfig(configObj.toJSONString());
			notifyMapper.insertNotifyPolicy(notifyPolicyVo);
			return notifyPolicyVo;
		}
	}
	
	@Override
	public Object myDoTest(JSONObject jsonObj) {
		String handler = jsonObj.getString("handler");
		INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(handler);
		if(notifyPolicyHandler == null) {
			throw new NotifyPolicyHandlerNotFoundException(handler);
		}
		Long id = jsonObj.getLong("id");
		String name = jsonObj.getString("name");
		if(id != null) {
			NotifyPolicyVo notifyPolicyVo = NotifyPolicyFactory.notifyPolicyMap.get(id);
			if(notifyPolicyVo == null) {
				throw new NotifyPolicyNotFoundException(id.toString());
			}
			notifyPolicyVo.setName(name);
			notifyPolicyVo.setLcd(new Date());
			notifyPolicyVo.setLcu(UserContext.get().getUserUuid(true));
			notifyPolicyVo.setLcuName(UserContext.get().getUserName());
			return notifyPolicyVo;
		}else {
			
			NotifyPolicyVo notifyPolicyVo = new NotifyPolicyVo(name, handler);
			notifyPolicyVo.setFcd(new Date());
			notifyPolicyVo.setFcu(UserContext.get().getUserUuid(true));
			notifyPolicyVo.setFcuName(UserContext.get().getUserName());
			JSONObject configObj = new JSONObject();
			JSONArray triggerList = new JSONArray();
			for (ValueTextVo notifyTrigger : notifyPolicyHandler.getNotifyTriggerList()) {
				JSONObject triggerObj = new JSONObject();
				triggerObj.put("trigger", notifyTrigger.getValue());
				triggerObj.put("triggerName", notifyTrigger.getText());
				triggerObj.put("notifyList", new JSONArray());
				triggerList.add(triggerObj);
			}
			configObj.put("triggerList", triggerList);			
			configObj.put("paramList", new JSONArray());
			configObj.put("templateList", new JSONArray());
			notifyPolicyVo.setConfig(configObj.toJSONString());
			NotifyPolicyFactory.notifyPolicyMap.put(notifyPolicyVo.getId(), notifyPolicyVo);
			return notifyPolicyVo;
		}
	}

}
