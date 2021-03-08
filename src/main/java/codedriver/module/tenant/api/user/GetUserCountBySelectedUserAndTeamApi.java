package codedriver.module.tenant.api.user;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.service.UserService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetUserCountBySelectedUserAndTeamApi extends PrivateApiComponentBase {

	@Resource
	private UserService userService;

	@Override
	public String getToken() {
		return "user/countofselecteduserandteam/get";
	}

	@Override
	public String getName() {
		return "根据选中的用户和分组计算激活的用户数";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "userUuidList", type = ApiParamType.JSONARRAY, desc = "用户uuid"),
			@Param(name = "teamUuidList", type = ApiParamType.JSONARRAY, desc = "分组uuid")
	})
	@Output({ @Param(name = "count", type = ApiParamType.INTEGER, desc = "用户数") })
	@Description(desc = "根据选中的用户和分组计算激活的用户数")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject result = new JSONObject();
		List<String> userUuidList = JSON.parseArray(jsonObj.getString("userUuidList"), String.class);
		List<String> teamUuidList = JSON.parseArray(jsonObj.getString("teamUuidList"), String.class);
		Set<String> uuidList = userService.getUserUuidSetByUserUuidListAndTeamUuidList(userUuidList,teamUuidList);
        result.put("count",uuidList.size());
		return result;
	}
}
