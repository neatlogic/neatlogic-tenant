package codedriver.module.tenant.api.notify;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.exception.user.UserNotFoundException;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@Transactional
@IsActived
public class NotifyPolicyExceptionNotifyDeleteApi extends ApiComponentBase {
	
	@Autowired
	private NotifyMapper notifyMapper;
	
	@Autowired
	private UserMapper userMapper;

	@Override
	public String getToken() {
		return "notify/policy/exceptionnotify/delete";
	}

	@Override
	public String getName() {
		return "通知策略异常通知删除接口";
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
		String userUuid = jsonObj.getString("userUuid");
		if(userMapper.checkUserIsExists(userUuid) == 0) {
			throw new UserNotFoundException(userUuid);
		}
		JSONObject config = notifyPolicyVo.getConfig();
		List<String> adminUserUuidList = JSON.parseArray(config.getJSONArray("adminUserUuidList").toJSONString(), String.class);
		adminUserUuidList.remove(userUuid);
		config.put("adminUserUuidList", adminUserUuidList);
		notifyPolicyVo.setConfig(config.toJSONString());
		notifyMapper.updateNotifyPolicyById(notifyPolicyVo);
		return null;
	}

}
