package codedriver.module.tenant.api.apimanage;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.exception.type.ApiNotFoundException;
import codedriver.framework.exception.type.ApiRepeatException;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.module.tenant.auth.label.INTERFACE_MODIFY;
import org.apache.commons.collections4.CollectionUtils;
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
import codedriver.framework.restful.core.privateapi.PrivateApiComponentFactory;
import codedriver.framework.restful.core.publicapi.PublicApiComponentFactory;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.restful.dao.mapper.ApiMapper;
import codedriver.framework.restful.dto.ApiHandlerVo;
import codedriver.framework.restful.dto.ApiVo;

import java.util.List;

@Service
@Transactional
@AuthAction(action = INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class ApiManageSaveApi extends PrivateApiComponentBase {

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
		@Param(name = "token", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\{\\}\\d/]+$", isRequired = true, desc = "token"),
		@Param(name = "name", type = ApiParamType.STRING, maxLength = 50, isRequired = true, desc = "名称"),
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
		@Param(name = "description", type = ApiParamType.STRING, desc = "描述"),
        @Param(name = "apiType", type = ApiParamType.STRING, desc = "API类型", isRequired = true),
        @Param(name = "operationType", type = ApiParamType.STRING, desc = "操作类型(create|update)", isRequired = true)
	})
	@Description(desc = "接口配置信息保存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ApiVo apiVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ApiVo>() {});
		String operationType = jsonObj.getString("operationType");

		ApiHandlerVo apiHandlerVo = PrivateApiComponentFactory.getApiHandlerByHandler(apiVo.getHandler());
		ApiVo ramApiVo = PrivateApiComponentFactory.getApiByToken(apiVo.getToken());
		if(apiHandlerVo == null) {
		    apiHandlerVo = PublicApiComponentFactory.getApiHandlerByHandler(apiVo.getHandler());
		    if(apiHandlerVo == null) {
		        throw new ComponentNotFoundException("接口组件:" + apiVo.getHandler() + "不存在");
		    }
		    ramApiVo = PublicApiComponentFactory.getApiByToken(apiVo.getToken());
		}

		apiVo.setType(apiHandlerVo.getType());
		apiVo.setModuleId(apiHandlerVo.getModuleId());

		if(ApiVo.ApiType.CUSTOM.getValue().equals(apiVo.getApiType())){
			if(ramApiVo != null){
				throw new ApiRepeatException("不可与系统接口使用同一个token");
			}
			List<ApiVo> dbApiList = ApiMapper.getAllApi();
			//校验token是否与自定义接口重复
			boolean isTokenRepeat = false;
			//如果添加自定义接口，就要校验当前token是否与其余自定义接口重复
			if(OperationTypeEnum.CREATE.getValue().equals(operationType)){
				if(CollectionUtils.isNotEmpty(dbApiList) && !isTokenRepeat){
					for(ApiVo api : dbApiList){
						if(api.getToken().equals(apiVo.getToken())){
							isTokenRepeat = true;
							break;
						}
					}
				}
			}
			if(isTokenRepeat){
				throw new ApiRepeatException("接口地址：" + apiVo.getToken() + "已存在");
			}

			boolean isNameRepeat = false;
			//校验接口名称是否与系统接口重复
			for(ApiVo api : PrivateApiComponentFactory.getApiList()){
				if(api.getName().equals(apiVo.getName())){
					isNameRepeat = true;
					break;
				}
			}
			//校验接口名称是否与其他自定义接口重复
//			List<ApiVo> dbApiList = ApiMapper.getAllApi();
			if(CollectionUtils.isNotEmpty(dbApiList) && !isNameRepeat){
				for(ApiVo api : dbApiList){
					if(api.getName().equals(apiVo.getName()) && !api.getToken().equals(apiVo.getToken())){
						isNameRepeat = true;
						break;
					}
				}
			}

			if(isNameRepeat){
				throw new ApiRepeatException("接口名称：" + apiVo.getName() + "已被占用");
			}
			ApiMapper.replaceApi(apiVo);
			return null;
        }else if(ApiVo.ApiType.SYSTEM.getValue().equals(apiVo.getApiType())){
            if(ramApiVo == null) {
                throw new ApiNotFoundException("此接口不存在");
            }
            if(ramApiVo.equals(apiVo)) {
                //如果接口配置信息与内存中接口配置信息一致，删除数据库中该接口的配置数据
                ApiMapper.deleteApiByToken(apiVo.getToken());
            }else {
                ApiMapper.replaceApi(apiVo);
            }
        }

//		ApiVo ramApiVo = ApiComponentFactory.getApiByToken(apiVo.getToken());
//		if(ramApiVo == null) {
//			ApiMapper.replaceApi(apiVo);
//			return null;
//		}
//		if(ramApiVo.equals(apiVo)) {
//			//如果接口配置信息与内存中接口配置信息一致，删除数据库中该接口的配置数据
//			ApiMapper.deleteApiByToken(apiVo.getToken());
//		}else {
//			ApiMapper.replaceApi(apiVo);
//		}
		return null;
	}

}
