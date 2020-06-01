package codedriver.module.tenant.api.notify;

import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.notify.core.INotifyPolicyHandler;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.dto.NotifyPolicyParamVo;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
public class NotifyPolicyGetApi  extends ApiComponentBase {

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
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "策略uuid")
	})
	@Output({
		@Param(name = "notifyPolicy", explode = NotifyPolicyVo.class, desc = "策略信息")
	})
	@Description(desc = "通知策略信息获取接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return null;
	}
	
	@Override
	public Object myDoTest(JSONObject jsonObj) {
		String uuid = jsonObj.getString("uuid");
		NotifyPolicyVo notifyPolicyVo = NotifyPolicyVo.notifyPolicyMap.get(uuid);
		if(notifyPolicyVo == null) {
			throw new NotifyPolicyNotFoundException(uuid);
		}
		JSONObject configObj = notifyPolicyVo.getConfigObj();
		List<NotifyPolicyParamVo> paramList = JSON.parseArray(configObj.getJSONArray("paramList").toJSONString(), NotifyPolicyParamVo.class);
		INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyVo.getPolicyHandler());
		if(notifyPolicyHandler == null) {
			throw new NotifyPolicyHandlerNotFoundException(notifyPolicyVo.getPolicyHandler());
		}
		paramList.addAll(notifyPolicyHandler.getSystemParamList());
		configObj.put("paramList", paramList);
		notifyPolicyVo.setConfig(configObj.toJSONString());
		return notifyPolicyVo;
	}

}