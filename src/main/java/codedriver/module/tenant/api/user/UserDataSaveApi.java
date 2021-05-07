/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.user;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserDataVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service

@OperationType(type = OperationTypeEnum.CREATE)
public class UserDataSaveApi extends PrivateApiComponentBase {

	@Autowired
	UserMapper userMapper;


	@Override
	public String getToken() {
		return "user/data/save";
	}

	@Override
	public String getName() {
		return "保存用户数据";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({@Param(name = "type", type = ApiParamType.STRING, desc = "功能类型，如果是用户默认模块数据，则应指定为defaultModulePage",
			isRequired = true)
	})
	@Output({})
	@Description(desc = "保存用户数据")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		UserDataVo userDataVo = new UserDataVo();
		String userUuid = UserContext.get().getUserUuid(true);
		String type = jsonObj.getString("type");
		userDataVo.setUserUuid(userUuid);
		userDataVo.setData(jsonObj.toJSONString());
		userDataVo.setType(type);

		if(userMapper.getUserDataByUserUuidAndType(userUuid,type) == null){
			userMapper.insertUserData(userDataVo);
		}else{
			userMapper.updateUserData(userDataVo);
		}
		return null;
	}
}
