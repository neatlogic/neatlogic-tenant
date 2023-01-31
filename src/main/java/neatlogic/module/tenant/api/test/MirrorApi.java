/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.tenant.api.test;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

@Deprecated

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
