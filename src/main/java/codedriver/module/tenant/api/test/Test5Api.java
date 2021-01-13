package codedriver.module.tenant.api.test;

import codedriver.framework.restful.core.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.restful.core.publicapi.PublicApiComponentBase;
@Service
@Deprecated
@OperationType(type = OperationTypeEnum.SEARCH)
public class Test5Api extends PublicApiComponentBase {
	
	@Override
	public String getName() {
		return "测试5";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return "test5";
	}

}
