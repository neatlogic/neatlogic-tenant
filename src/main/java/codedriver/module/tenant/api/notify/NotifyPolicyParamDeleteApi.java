package codedriver.module.tenant.api.notify;

import java.util.Iterator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.notify.dto.NotifyPolicyParamVo;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@Transactional
public class NotifyPolicyParamDeleteApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "notify/policy/param/delete";
	}

	@Override
	public String getName() {
		return "通知策略参数删除接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "policyUuid", type = ApiParamType.STRING, isRequired = true, desc = "策略uuid"),
		@Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "参数名")
	})
	@Output({
		@Param(name = "paramList", explode = NotifyPolicyParamVo[].class, desc = "参数列表")
	})
	@Description(desc = "通知策略参数删除接口")
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
		String name = jsonObj.getString("name");
		JSONObject configObj = notifyPolicyVo.getConfigObj();
		List<NotifyPolicyParamVo> paramList = JSON.parseArray(configObj.getJSONArray("paramList").toJSONString(), NotifyPolicyParamVo.class);
		Iterator<NotifyPolicyParamVo> iterator = paramList.iterator();
		while(iterator.hasNext()) {
			NotifyPolicyParamVo notifyPolicyParamVo = iterator.next();
			if(name.equals(notifyPolicyParamVo.getName())) {
				iterator.remove();
			}
		}
		configObj.put("paramList", paramList);
		notifyPolicyVo.setConfig(configObj.toJSONString());
		JSONObject resultObj = new JSONObject();
		resultObj.put("paramList", paramList);
		return resultObj;
	}
}
