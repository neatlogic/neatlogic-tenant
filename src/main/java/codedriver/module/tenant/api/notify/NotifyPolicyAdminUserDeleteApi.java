package codedriver.module.tenant.api.notify;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

public class NotifyPolicyAdminUserDeleteApi extends ApiComponentBase {
	
	@Autowired
	private NotifyMapper notifyMapper;

	@Override
	public String getToken() {
		return "notify/policy/adminuser/delete";
	}

	@Override
	public String getName() {
		return "通知策略管理员删除接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "policyId", type = ApiParamType.LONG, isRequired = true, desc = "策略id"),
		@Param(name = "userUuid", type = ApiParamType.STRING, isRequired = true, desc = "用户uuid")
	})
	@Description(desc = "通知策略管理员删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long policyId = jsonObj.getLong("policyId");
		NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
		if(notifyPolicyVo == null) {
			throw new NotifyPolicyNotFoundException(policyId.toString());
		}
		return null;
	}

}
