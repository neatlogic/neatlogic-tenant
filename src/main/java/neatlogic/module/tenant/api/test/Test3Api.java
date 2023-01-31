package neatlogic.module.tenant.api.test;

import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.OperationType;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.restful.core.publicapi.PublicApiComponentBase;
@Deprecated
@OperationType(type = OperationTypeEnum.SEARCH)
@Service
public class Test3Api extends PublicApiComponentBase {
	
	@Override
	public String getName() {
		return "测试3";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return "test3";
	}

}
