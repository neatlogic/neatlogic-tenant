package codedriver.module.tenant.api.user;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserDataVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
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

	@Input({@Param(name = "type", type = ApiParamType.STRING, desc = "功能类型，如果是用户默认模块数据，则应指定为defaultModulePage",
			isRequired = true)
	})
	@Output({})
	@Description(desc = "保存用户数据接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		UserDataVo userDataVoVo = new UserDataVo();
		String userUuid = UserContext.get().getUserUuid(true);

		userDataVoVo.setUserUuid(userUuid);
		userDataVoVo.setData(jsonObj.toJSONString());
		String type = jsonObj.getString("type");
		userDataVoVo.setType(type);

		UserDataVo userDataVo = userMapper.getUserDataByUserUuidAndType(userUuid,type);
		if(userDataVo == null){
			userMapper.insertUserData(userDataVoVo);
		}else{
			userDataVo.setData(jsonObj.toJSONString());
			userMapper.updateUserData(userDataVo);
		}
		return null;
	}
}
