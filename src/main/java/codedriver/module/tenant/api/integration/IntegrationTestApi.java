package codedriver.module.tenant.api.integration;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.module.tenant.auth.label.INTEGRATION_MODIFY;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.integration.IntegrationHandlerNotFoundException;
import codedriver.framework.integration.core.IIntegrationHandler;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.integration.handler.FrameworkRequestFrom;

import java.util.HashSet;
import java.util.Set;

@Service
@AuthAction(action = INTEGRATION_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class IntegrationTestApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		return "integration/test";
	}

	@Override
	public String getName() {
		return "集成配置测试接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "url", type = ApiParamType.STRING, desc = "目标地址", isRequired = true, rule = "^((http|ftp|https)://)(([a-zA-Z0-9\\._-]+)|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?"),
			@Param(name = "handler", type = ApiParamType.STRING, desc = "组件", isRequired = true, xss = true),
			@Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "配置，json格式", isRequired = true)
	})
	@Description(desc = "集成配置测试接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		IntegrationVo integrationVo = JSONObject.toJavaObject(jsonObj, IntegrationVo.class);
		IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
		if (handler == null) {
			throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
		}
		IntegrationResultVo resultVo = handler.sendRequest(integrationVo, FrameworkRequestFrom.TEST);
		if(IIntegrationHandler.Type.MATRIX.getValue().equals(handler.getType())
				&& StringUtils.isBlank(resultVo.getError()) && StringUtils.isNotBlank(resultVo.getTransformedResult())){
			JSONObject transformedResult = null;
			try {
				transformedResult = JSONObject.parseObject(resultVo.getTransformedResult());
			}catch (Exception ex){
				resultVo.appendError("返回结果不是JSON格式");
				return resultVo;
			}
			if(MapUtils.isNotEmpty(transformedResult)){
				Set<String> keys = transformedResult.keySet();
				Set<String> keySet = new HashSet<>();
				handler.getOutputPattern().stream().forEach(o -> keySet.add(o.getName()));
				if(!CollectionUtils.containsAll(keys,keySet)){
					resultVo.appendError("返回结果不符合格式，缺少" + JSON.toJSONString(CollectionUtils.removeAll(keySet, keys)));
					return resultVo;
				}
				JSONArray theadList = transformedResult.getJSONArray("theadList");
				if(CollectionUtils.isNotEmpty(theadList)){
					for(int i = 0; i < theadList.size();i++){
						if(!theadList.getJSONObject(i).containsKey("key") || !theadList.getJSONObject(i).containsKey("title")){
							resultVo.appendError("返回结果不符合格式,theadList缺少key或title");
							return resultVo;
						}
					}
				}else{
					resultVo.appendError("返回结果不符合格式,缺少theadList");
					return resultVo;
				}
			}
		}
		return resultVo;
	}
}
