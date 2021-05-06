package codedriver.module.tenant.api.user;

import java.util.Random;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.FRAMEWORK_BASE;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

@Service
@AuthAction(action = FRAMEWORK_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class CurrentUserGetApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		return "/user/current/get";
	}

	@Override
	public String getName() {
		return "获取当前用户test接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({})
	@Output({})
	@Description(desc = "获取当前用户test接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Random random = new Random();
		Thread.sleep(random.nextInt(10000));
		return UserContext.get();
	}
}
