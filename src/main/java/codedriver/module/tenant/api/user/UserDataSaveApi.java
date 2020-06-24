package codedriver.module.tenant.api.user;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserDataVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.core.ApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserDataSaveApi extends ApiComponentBase {

	@Autowired
	UserMapper userMapper;


	@Override
	public String getToken() {
		return "user/data/save";
	}

	@Override
	public String getName() {
		return "保存用户数据接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({})
	@Output({})
	@Description(desc = "保存用户数据接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		UserDataVo userDataVoVo = new UserDataVo();
		String userUuid = jsonObj.getString("userUuid");
		if (org.apache.commons.lang3.StringUtils.isBlank(userUuid)) {
			userUuid = UserContext.get().getUserUuid(true);
		}
		userDataVoVo.setUserUuid(userUuid);
		userDataVoVo.setData(jsonObj.toJSONString());
		//如果jsonObj没有指定type，则使用默认值defaultModule
		String type = jsonObj.get("type") == null ? "defaultModule": jsonObj.get("type").toString();
		userDataVoVo.setType(type);

		UserDataVo userDataVo = userMapper.getUserDataByUserUuidAndType(UserContext.get().getUserUuid(),type);
		if(userDataVo == null){
			userMapper.insertUserData(userDataVoVo);
		}
		return null;
	}
}
