package codedriver.module.tenant.api.user;

import java.util.ArrayList;
import java.util.List;

import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class UserListApi extends PrivateApiComponentBase {

	@Autowired
	private UserMapper userMapper;
	
	@Override
	public String getToken() {
		return "user/list";
	}

	@Override
	public String getName() {
		return "用户列表获取接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "teamUuidList", type = ApiParamType.JSONARRAY, desc = "用户组uuid集合"),
		@Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", desc = "用户是否激活")
	})
	@Output({
		@Param(name = "userList", type = ApiParamType.JSONARRAY, desc = "用户集合")
	})
	@Description(desc = "用户列表获取接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		List<String> teamUuidList = JSON.parseArray(JSON.toJSONString(jsonObj.getJSONArray("teamUuidList")), String.class);
		Integer isActive = jsonObj.getInteger("isActive");
		if(CollectionUtils.isNotEmpty(teamUuidList)) {
			List<String> userUuidList = userMapper.getUserUuidListByTeamUuidList(teamUuidList);
			if(CollectionUtils.isNotEmpty(userUuidList)) {
				return userMapper.getUserListByUserUuidList(userUuidList,isActive);
			}
		}
		return new ArrayList<>();
	}

}
