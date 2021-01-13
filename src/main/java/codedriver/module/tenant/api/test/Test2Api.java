package codedriver.module.tenant.api.test;

import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.core.publicapi.PublicApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
@Deprecated
@OperationType(type = OperationTypeEnum.SEARCH)
@Service
public class Test2Api extends PublicApiComponentBase {
	
	@Override
	public String getName() {
		return "测试2";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return "test2";
	}

}
