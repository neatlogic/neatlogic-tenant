package codedriver.module.tenant.api.notify;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.notify.core.NotifyPolicyFactory;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyPolicyParamVo;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@Transactional
@IsActived
public class NotifyPolicyParamSaveApi extends ApiComponentBase {
	
	@Autowired
	private NotifyMapper notifyMapper;

	@Override
	public String getToken() {
		return "notify/policy/param/save";
	}

	@Override
	public String getName() {
		return "通知策略参数保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "policyId", type = ApiParamType.LONG, isRequired = true, desc = "策略id"),
		@Param(name = "handler", type = ApiParamType.STRING, isRequired = true, desc = "参数名"),
		@Param(name = "basicType", type = ApiParamType.STRING, isRequired = true, desc = "参数类型"),
		@Param(name = "handlerName", type = ApiParamType.STRING, isRequired = true, desc = "参数描述")
	})
	@Output({
		@Param(name = "paramList", explode = NotifyPolicyParamVo[].class, desc = "参数列表")
	})
	@Description(desc = "通知策略参数保存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long policyId = jsonObj.getLong("policyId");
		NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
		if(notifyPolicyVo == null) {
			throw new NotifyPolicyNotFoundException(policyId.toString());
		}
		String handler = jsonObj.getString("handler");
		String basicType = jsonObj.getString("basicType");
		String handlerName = jsonObj.getString("handlerName");
		boolean isNew = true;
		JSONObject configObj = notifyPolicyVo.getConfigObj();
		List<NotifyPolicyParamVo> paramList = JSON.parseArray(configObj.getJSONArray("paramList").toJSONString(), NotifyPolicyParamVo.class);
		for(NotifyPolicyParamVo notifyPolicyParamVo : paramList) {
			if(handler.equals(notifyPolicyParamVo.getHandler())) {
				notifyPolicyParamVo.setBasicType(basicType);
				notifyPolicyParamVo.setHandlerName(handlerName);
				isNew = false;
			}
		}
		if(isNew) {
			NotifyPolicyParamVo notifyPolicyParamVo = new NotifyPolicyParamVo();
			notifyPolicyParamVo.setHandler(handler);
			notifyPolicyParamVo.setBasicType(basicType);
			notifyPolicyParamVo.setHandlerName(handlerName);
			paramList.add(notifyPolicyParamVo);
		}
		paramList.sort((e1, e2) -> e1.getHandler().compareToIgnoreCase(e2.getHandler()));
		configObj.put("paramList", paramList);
		notifyPolicyVo.setConfig(configObj.toJSONString());
		notifyMapper.updateNotifyPolicyById(notifyPolicyVo);
		JSONObject resultObj = new JSONObject();
		resultObj.put("paramList", paramList);
		return resultObj;
	}


	@Override
	public Object myDoTest(JSONObject jsonObj) {
		Long policyId = jsonObj.getLong("policyId");
		NotifyPolicyVo notifyPolicyVo = NotifyPolicyFactory.notifyPolicyMap.get(policyId);
		if(notifyPolicyVo == null) {
			throw new NotifyPolicyNotFoundException(policyId.toString());
		}
		String handler = jsonObj.getString("handler");
		String basicType = jsonObj.getString("basicType");
		String handlerName = jsonObj.getString("handlerName");
		boolean isNew = true;
		JSONObject configObj = notifyPolicyVo.getConfigObj();
		List<NotifyPolicyParamVo> paramList = JSON.parseArray(configObj.getJSONArray("paramList").toJSONString(), NotifyPolicyParamVo.class);
		for(NotifyPolicyParamVo notifyPolicyParamVo : paramList) {
			if(handler.equals(notifyPolicyParamVo.getHandler())) {
				notifyPolicyParamVo.setBasicType(basicType);
				notifyPolicyParamVo.setHandlerName(handlerName);
				isNew = false;
			}
		}
		if(isNew) {
			NotifyPolicyParamVo notifyPolicyParamVo = new NotifyPolicyParamVo();
			notifyPolicyParamVo.setHandler(handler);
			notifyPolicyParamVo.setBasicType(basicType);
			notifyPolicyParamVo.setHandlerName(handlerName);
			paramList.add(notifyPolicyParamVo);
		}
		paramList.sort((e1, e2) -> e1.getHandler().compareToIgnoreCase(e2.getHandler()));
		configObj.put("paramList", paramList);
		notifyPolicyVo.setConfig(configObj.toJSONString());
		JSONObject resultObj = new JSONObject();
		resultObj.put("paramList", paramList);
		return resultObj;
	}
}
