package neatlogic.module.tenant.api.test;

import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.OperationType;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.restful.core.publicapi.PublicApiComponentBase;
@Service
@Deprecated
@OperationType(type = OperationTypeEnum.SEARCH)
public class Test4Api extends PublicApiComponentBase {

	@Override
	public String getName() {
		return "测试4";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return "test4";
	}

}
