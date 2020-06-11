package codedriver.module.tenant.api.notify;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.notify.core.INotifyPolicyHandler;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyPolicyParamVo;
import codedriver.framework.notify.dto.NotifyPolicyVo;
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
public class NotifyPolicyGetApi  extends ApiComponentBase {
	
	@Autowired
	private NotifyMapper notifyMapper;
	
	@Autowired
	private UserMapper userMapper;

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
		@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "策略id")
	})
	@Output({
		@Param(explode = NotifyPolicyVo.class, desc = "策略信息")
	})
	@Description(desc = "通知策略信息获取接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long id = jsonObj.getLong("id");
		NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(id);
		if(notifyPolicyVo == null) {
			throw new NotifyPolicyNotFoundException(id.toString());
		}
		JSONObject config = notifyPolicyVo.getConfig();
		List<NotifyPolicyParamVo> paramList = JSON.parseArray(config.getJSONArray("paramList").toJSONString(), NotifyPolicyParamVo.class);
		INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyVo.getHandler());
		if(notifyPolicyHandler == null) {
			throw new NotifyPolicyHandlerNotFoundException(notifyPolicyVo.getHandler());
		}
		paramList.addAll(notifyPolicyHandler.getSystemParamList());
		paramList.sort((e1, e2) -> e1.getHandler().compareToIgnoreCase(e2.getHandler()));
		config.put("paramList", paramList);
		List<String> adminUserUuidList = JSON.parseArray(config.getJSONArray("adminUserUuidList").toJSONString(), String.class);
		if(CollectionUtils.isNotEmpty(adminUserUuidList)) {
			List<UserVo> userList = userMapper.getUserByUserUuidList(adminUserUuidList);
			config.put("userList", userList);
		}else {
			config.put("userList", new JSONArray());
		}
		notifyPolicyVo.setConfig(config.toJSONString());
		return notifyPolicyVo;
	}

}