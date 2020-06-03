package codedriver.module.tenant.api.notify;

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
import codedriver.framework.notify.exception.NotifyTemplateNotFoundException;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@Transactional
@IsActived
public class NotifyPolicyTemplateSaveApi extends ApiComponentBase {

	@Autowired
	private NotifyMapper notifyMapper;

	@Override
	public String getToken() {
		return "notify/policy/template/save";
	}

	@Override
	public String getName() {
		return "通知模板保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Input({
		@Param(name = "policyId", type = ApiParamType.LONG, isRequired = true, desc = "策略id"),
		@Param(name = "id", type = ApiParamType.LONG, desc = "模板id"),
		@Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "模板名称"),
		@Param(name = "title", type = ApiParamType.STRING, isRequired = true, desc = "模板标题"),
		@Param(name = "content", type = ApiParamType.STRING, isRequired = true, desc = "模板内容")
	})
	@Output({
		@Param(name = "templateList", explode = NotifyTemplateVo[].class, desc = "通知模板列表")
	})
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
		String name = jsonObj.getString("name");
		String title = jsonObj.getString("title");
		String content = jsonObj.getString("content");
		JSONObject configObj = notifyPolicyVo.getConfigObj();
		List<NotifyTemplateVo> templateList = JSON.parseArray(configObj.getJSONArray("templateList").toJSONString(), NotifyTemplateVo.class);
		if(id != null) {
			boolean isExists = false;
			for(NotifyTemplateVo notifyTemplateVo : templateList) {
				if(id.equals(notifyTemplateVo.getId())) {
					notifyTemplateVo.setName(name);
					notifyTemplateVo.setTitle(title);
					notifyTemplateVo.setContent(content);
					isExists = true;
				}
			}
			if(!isExists) {
				throw new NotifyTemplateNotFoundException(id.toString());
			}
		}else {
			NotifyTemplateVo notifyTemplateVo = new NotifyTemplateVo();
			notifyTemplateVo.setName(name);
			notifyTemplateVo.setTitle(title);
			notifyTemplateVo.setContent(content);
			templateList.add(notifyTemplateVo);
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
		String name = jsonObj.getString("name");
		String title = jsonObj.getString("title");
		String content = jsonObj.getString("content");
		JSONObject configObj = notifyPolicyVo.getConfigObj();
		List<NotifyTemplateVo> templateList = JSON.parseArray(configObj.getJSONArray("templateList").toJSONString(), NotifyTemplateVo.class);
		if(id != null) {
			boolean isExists = false;
			for(NotifyTemplateVo notifyTemplateVo : templateList) {
				if(id.equals(notifyTemplateVo.getId())) {
					notifyTemplateVo.setName(name);
					notifyTemplateVo.setTitle(title);
					notifyTemplateVo.setContent(content);
					isExists = true;
				}
			}
			if(!isExists) {
				throw new NotifyTemplateNotFoundException(id.toString());
			}
		}else {
			NotifyTemplateVo notifyTemplateVo = new NotifyTemplateVo();
			notifyTemplateVo.setName(name);
			notifyTemplateVo.setTitle(title);
			notifyTemplateVo.setContent(content);
			templateList.add(notifyTemplateVo);
		}
		configObj.put("templateList", templateList);
		notifyPolicyVo.setConfig(configObj.toJSONString());
		JSONObject resultObj = new JSONObject();
		resultObj.put("templateList", templateList);
		return resultObj;
	}
}
