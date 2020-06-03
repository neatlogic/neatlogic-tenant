package codedriver.module.tenant.api.notify;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@IsActived
public class NotifyPolicyTemplateListApi extends ApiComponentBase {

	@Autowired
	private NotifyMapper notifyMapper;

	@Override
	public String getToken() {
		return "notify/policy/template/list";
	}

	@Override
	public String getName() {
		return "通知策略模板列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "模糊匹配"),
		@Param(name = "policyId", type = ApiParamType.LONG, isRequired = true, desc = "策略id"),
		@Param(name = "notifyHandler", type = ApiParamType.STRING, desc = "通知处理器")
	})
	@Output({
		@Param(name = "templateList", explode = NotifyTemplateVo[].class, desc = "通知模板列表")
	})
	@Description(desc = "通知策略模板列表接口")
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
		
		String keyword = jsonObj.getString("keyword");
		JSONObject configObj = notifyPolicyVo.getConfigObj();
		List<NotifyTemplateVo> templateList = new ArrayList<>();
		if(StringUtils.isNotBlank(keyword)) {
			keyword = keyword.toLowerCase();
			for(NotifyTemplateVo notifyTemplateVo : JSON.parseArray(configObj.getJSONArray("templateList").toJSONString(), NotifyTemplateVo.class)) {
				if(notifyTemplateVo.getName().toLowerCase().contains(keyword)) {
					templateList.add(notifyTemplateVo);
				}
			}
		}else {
			templateList = JSON.parseArray(configObj.getJSONArray("templateList").toJSONString(), NotifyTemplateVo.class);
		}
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
		
		String keyword = jsonObj.getString("keyword");
		JSONObject configObj = notifyPolicyVo.getConfigObj();
		List<NotifyTemplateVo> templateList = new ArrayList<>();
		if(StringUtils.isNotBlank(keyword)) {
			keyword = keyword.toLowerCase();
			for(NotifyTemplateVo notifyTemplateVo : JSON.parseArray(configObj.getJSONArray("templateList").toJSONString(), NotifyTemplateVo.class)) {
				if(notifyTemplateVo.getName().toLowerCase().contains(keyword)) {
					templateList.add(notifyTemplateVo);
				}
			}
		}else {
			templateList = JSON.parseArray(configObj.getJSONArray("templateList").toJSONString(), NotifyTemplateVo.class);
		}
		JSONObject resultObj = new JSONObject();
		resultObj.put("templateList", templateList);
		return resultObj;
	}
}
