package codedriver.module.tenant.api.notify;

import java.util.ArrayList;
import java.util.List;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.type.ParamIrregularException;
import codedriver.framework.exception.user.UserNotFoundException;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
public class NotifyPolicyExceptionNotifyAddApi extends ApiComponentBase {
	
	@Autowired
	private NotifyMapper notifyMapper;
	
	@Autowired
	private UserMapper userMapper;

	@Override
	public String getToken() {
		return "notify/policy/exceptionnotify/add";
	}

	@Override
	public String getName() {
		return "通知策略异常通知添加接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "policyId", type = ApiParamType.LONG, isRequired = true, desc = "策略id"),
		@Param(name = "userUuidList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "用户uuid列表，格式[\"user#uuid\"]")
	})
	@Output({
		@Param(name = "userList", explode = UserVo[].class, desc = "用户信息列表")
	})
	@Description(desc = "通知策略异常通知添加接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject resultObj = new JSONObject();
		Long policyId = jsonObj.getLong("policyId");
		NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
		if(notifyPolicyVo == null) {
			throw new NotifyPolicyNotFoundException(policyId.toString());
		}
		JSONObject config = notifyPolicyVo.getConfig();
		List<String> adminUserUuidList = JSON.parseArray(config.getJSONArray("adminUserUuidList").toJSONString(), String.class);
		List<String> userUuidList = JSON.parseArray(jsonObj.getJSONArray("userUuidList").toJSONString(), String.class);
		List<UserVo> userList = new ArrayList<>();
		for(String userUuid : userUuidList) {
			if(userUuid.startsWith(GroupSearch.USER.getValuePlugin())) {
				String uuid = userUuid.split("#")[1];
				UserVo user = userMapper.getUserBaseInfoByUuid(uuid);
				if(user == null) {
					throw new UserNotFoundException(uuid);
				}else {
					if(!adminUserUuidList.contains(uuid)) {
						userList.add(user);
						adminUserUuidList.add(uuid);
					}
				}
			}else {
				throw new ParamIrregularException("参数“userUuidList”不符合格式要求");
			}
		}
		config.put("adminUserUuidList", adminUserUuidList);
		notifyPolicyVo.setConfig(config.toJSONString());
		notifyMapper.updateNotifyPolicyById(notifyPolicyVo);
		resultObj.put("userList", userList);
		return resultObj;
	}

}
