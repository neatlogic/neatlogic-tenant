package codedriver.module.tenant.api.notify;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.notify.core.NotifyPolicyFactory;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@Transactional
public class NotifyPolicyDeleteApi  extends ApiComponentBase {
	
	@Autowired
	private NotifyMapper notifyMapper;

	@Override
	public String getToken() {
		return "notify/policy/delete";
	}

	@Override
	public String getName() {
		return "通知策略删除接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "策略id")
	})
	@Output({})
	@Description(desc = "通知策略删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long id = jsonObj.getLong("id");
		notifyMapper.deleteNotifyPolicyById(id);
		return null;
	}
	
	@Override
	public Object myDoTest(JSONObject jsonObj) {
		Long id = jsonObj.getLong("id");
		NotifyPolicyFactory.notifyPolicyMap.remove(id);
		return null;
	}

}
