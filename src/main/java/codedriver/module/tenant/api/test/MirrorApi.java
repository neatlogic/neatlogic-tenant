package codedriver.module.tenant.api.test;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.FRAMEWOKR_BASE;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

@Deprecated
@AuthAction(action = FRAMEWOKR_BASE.class)
@Component
public class MirrorApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		// TODO Auto-generated method stub
		return "test/mirror";
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "反射测试api";
	}

	@Override
	public String getConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRaw() {
		return true;
	}

	@Override
	@Description(desc = "反射测试api")
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return jsonObj;
	}

}
