package codedriver.module.tenant.api.apimanage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.exception.type.ApiNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.restful.core.ApiComponentFactory;
import codedriver.framework.restful.dao.mapper.ApiMapper;
import codedriver.framework.restful.dto.ApiVo;
import codedriver.module.tenant.exception.api.ApiNotAllowedToDeleteException;
@Service
@Transactional
public class ApiManageDeleteApi extends ApiComponentBase {
	
	@Autowired
	private ApiMapper ApiMapper;
	
	@Override
	public String getToken() {
		return "apimanage/delete";
	}

	@Override
	public String getName() {
		return "接口删除接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "token", type = ApiParamType.STRING, isRequired = true, xss = true, desc = "接口token")
	})
	@Description(desc = "接口删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String token = jsonObj.getString("token");
		ApiVo apiVo = ApiComponentFactory.getApiByToken(token);
		//内存中的接口不允许删除
		if(apiVo != null) {
			throw new ApiNotAllowedToDeleteException(token);
		}
		apiVo = ApiMapper.getApiByToken(token);
		if(apiVo == null) {
			throw new ApiNotFoundException("token为'" + token + "'的接口不存在");
		}
		ApiMapper.deleteApiByToken(token);
		return null;
	}

}
