package codedriver.module.tenant.api.notify;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.notify.core.INotifyPolicyHandler;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.dto.NotifyTemplateVo;
import codedriver.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;
import codedriver.framework.notify.exception.NotifyTemplateNotFoundException;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@Transactional
public class NotifyPolicyTemplateSaveApi extends ApiComponentBase {

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
		@Param(name = "policyUuid", type = ApiParamType.STRING, isRequired = true, desc = "策略uuid"),
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "模板uuid"),
		@Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "模板名称"),
		@Param(name = "title", type = ApiParamType.STRING, isRequired = true, desc = "模板标题"),
		@Param(name = "content", type = ApiParamType.STRING, isRequired = true, desc = "模板内容")
	})
	@Output({
		@Param(name = "templateList", explode = NotifyTemplateVo[].class, desc = "通知模板列表")
	})
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Object myDoTest(JSONObject jsonObj) {
		String policyUuid = jsonObj.getString("policyUuid");
		NotifyPolicyVo notifyPolicyVo = NotifyPolicyVo.notifyPolicyMap.get(policyUuid);
		if(notifyPolicyVo == null) {
			throw new NotifyPolicyNotFoundException(policyUuid);
		}
		INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyVo.getPolicyHandler());
		if(notifyPolicyHandler == null) {
			throw new NotifyPolicyHandlerNotFoundException(notifyPolicyVo.getPolicyHandler());
		}
		
		String uuid = jsonObj.getString("uuid");
		String name = jsonObj.getString("name");
		String title = jsonObj.getString("title");
		String content = jsonObj.getString("content");
		JSONObject configObj = notifyPolicyVo.getConfigObj();
		List<NotifyTemplateVo> templateList = JSON.parseArray(configObj.getString("templateList"), NotifyTemplateVo.class);
		if(StringUtils.isNotBlank(uuid)) {
			boolean isExists = false;
			for(NotifyTemplateVo notifyTemplateVo : templateList) {
				if(uuid.equals(notifyTemplateVo.getUuid())) {
					notifyTemplateVo.setName(name);
					notifyTemplateVo.setTitle(title);
					notifyTemplateVo.setContent(content);
					isExists = true;
				}
			}
			if(!isExists) {
				throw new NotifyTemplateNotFoundException(uuid);
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
