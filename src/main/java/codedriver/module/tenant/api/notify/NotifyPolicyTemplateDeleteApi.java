package codedriver.module.tenant.api.notify;

import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.notify.core.INotifyPolicyHandler;
import codedriver.framework.notify.core.NotifyPolicyFactory;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.dto.NotifyTemplateVo;
import codedriver.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@Transactional
public class NotifyPolicyTemplateDeleteApi extends ApiComponentBase {

	@Autowired
	private NotifyMapper notifyMapper;
	
	@Override
	public String getToken() {
		return "notify/policy/template/delete";
	}

	@Override
	public String getName() {
		return "通知模板删除接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "policyId", type = ApiParamType.LONG, isRequired = true, desc = "策略id"),
		@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "模板id")
	})
	@Output({
		@Param(name = "templateList", explode = NotifyTemplateVo[].class, desc = "通知模板列表")
	})
	@Description(desc = "通知模板删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long policyId = jsonObj.getLong("policyId");
		NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
		if(notifyPolicyVo == null) {
			throw new NotifyPolicyNotFoundException(policyId.toString());
		}
		INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyVo.getHandler());
		if(notifyPolicyHandler == null) {
			throw new NotifyPolicyHandlerNotFoundException(notifyPolicyVo.getHandler());
		}
		
		Long id = jsonObj.getLong("id");
		JSONObject configObj = notifyPolicyVo.getConfigObj();
		List<NotifyTemplateVo> templateList = JSON.parseArray(configObj.getJSONArray("templateList").toJSONString(), NotifyTemplateVo.class);
		Iterator<NotifyTemplateVo> iterator = templateList.iterator();
		while(iterator.hasNext()) {
			NotifyTemplateVo notifyTemplateVo = iterator.next();
			if(id.equals(notifyTemplateVo.getId())) {
				iterator.remove();
			}
		}
		configObj.put("templateList", templateList);
		notifyPolicyVo.setConfig(configObj.toJSONString());
		notifyMapper.updateNotifyPolicyById(notifyPolicyVo);
		JSONObject resultObj = new JSONObject();
		resultObj.put("templateList", templateList);
		return resultObj;
	}
	
	@Override
	public Object myDoTest(JSONObject jsonObj) {
		Long policyId = jsonObj.getLong("policyId");
		NotifyPolicyVo notifyPolicyVo = NotifyPolicyFactory.notifyPolicyMap.get(policyId);
		if(notifyPolicyVo == null) {
			throw new NotifyPolicyNotFoundException(policyId.toString());
		}
		INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyVo.getHandler());
		if(notifyPolicyHandler == null) {
			throw new NotifyPolicyHandlerNotFoundException(notifyPolicyVo.getHandler());
		}
		
		Long id = jsonObj.getLong("id");
		JSONObject configObj = notifyPolicyVo.getConfigObj();
		List<NotifyTemplateVo> templateList = JSON.parseArray(configObj.getJSONArray("templateList").toJSONString(), NotifyTemplateVo.class);
		Iterator<NotifyTemplateVo> iterator = templateList.iterator();
		while(iterator.hasNext()) {
			NotifyTemplateVo notifyTemplateVo = iterator.next();
			if(id.equals(notifyTemplateVo.getId())) {
				iterator.remove();
			}
		}
		configObj.put("templateList", templateList);
		notifyPolicyVo.setConfig(configObj.toJSONString());
		JSONObject resultObj = new JSONObject();
		resultObj.put("templateList", templateList);
		return resultObj;
	}

}
