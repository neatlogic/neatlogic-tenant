package codedriver.module.tenant.api.apimanage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.type.ComponentNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.restful.core.ApiComponentFactory;
import codedriver.framework.restful.dao.mapper.ApiMapper;
import codedriver.framework.restful.dto.ApiHandlerVo;
import codedriver.framework.restful.dto.ApiVo;

@Service
@Transactional
public class ApiManageSaveApi extends ApiComponentBase {

	@Autowired
	private ApiMapper ApiMapper;
	
	@Override
	public String getToken() {
		return "apimanage/save";
	}

	@Override
	public String getName() {
		return "接口配置信息保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "token", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d/]+$", isRequired = true, desc = "token"),
		@Param(name = "name", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", maxLength = 50, isRequired = true, desc = "名称"),
		@Param(name = "handler", type = ApiParamType.STRING, isRequired = true, desc = "处理器"),
		@Param(name = "needAudit", type = ApiParamType.ENUM, rule = "0,1", isRequired = true, desc = "是否保存调用记录"),
		@Param(name = "authtype", type = ApiParamType.STRING, isRequired = true, desc = "认证方式"),
		@Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", isRequired = true, desc = "是否激活"),
		@Param(name = "timeout", type = ApiParamType.INTEGER, desc = "请求时效"),
		@Param(name = "qps", type = ApiParamType.INTEGER, desc = "每秒访问几次，大于0生效"),
		@Param(name = "expire", type = ApiParamType.LONG, desc = "使用期限"),
		@Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "json格式,配置信息"),
		@Param(name = "username", type = ApiParamType.STRING, desc = "用户名称"),
		@Param(name = "password", type = ApiParamType.STRING, desc = "密码"),
		@Param(name = "description", type = ApiParamType.STRING, desc = "描述")
	})
	@Description(desc = "接口配置信息保存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ApiVo apiVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ApiVo>() {});
		
		ApiHandlerVo apiHandlerVo = ApiComponentFactory.getApiHandlerByHandler(apiVo.getHandler());
		if(apiHandlerVo == null) {
			throw new ComponentNotFoundException("接口组件:" + apiVo.getHandler() + "不存在");
		}

		ApiVo ramApiVo = ApiComponentFactory.getApiByToken(apiVo.getToken());
		if(ramApiVo == null) {
			ApiMapper.replaceApi(apiVo);
			return null;
		}
		if(ramApiVo.equals(apiVo)) {
			//如果接口配置信息与内存中接口配置信息一致，删除数据库中该接口的配置数据
			ApiMapper.deleteApiByToken(apiVo.getToken());
		}else {
			ApiMapper.replaceApi(apiVo);
		}	
		return null;
	}

}
