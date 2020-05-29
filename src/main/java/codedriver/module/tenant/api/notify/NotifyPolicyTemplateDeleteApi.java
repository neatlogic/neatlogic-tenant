package codedriver.module.tenant.api.notify;

import java.util.Iterator;
import java.util.List;

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
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@Transactional
public class NotifyPolicyTemplateDeleteApi extends ApiComponentBase {

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
		@Param(name = "policyUuid", type = ApiParamType.STRING, isRequired = true, desc = "策略uuid"),
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "模板uuid")
	})
	@Description(desc = "通知策略信息获取接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
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
		JSONObject configObj = notifyPolicyVo.getConfigObj();
		List<NotifyTemplateVo> templateList = JSON.parseArray(configObj.getString("templateList"), NotifyTemplateVo.class);
		Iterator<NotifyTemplateVo> iterator = templateList.iterator();
		while(iterator.hasNext()) {
			NotifyTemplateVo notifyTemplateVo = iterator.next();
			if(uuid.equals(notifyTemplateVo.getUuid())) {
				iterator.remove();
			}
		}
		configObj.put("templateList", templateList);
		notifyPolicyVo.setConfig(configObj.toJSONString());
		return null;
	}

}
