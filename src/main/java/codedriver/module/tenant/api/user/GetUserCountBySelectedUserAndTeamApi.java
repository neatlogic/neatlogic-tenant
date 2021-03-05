package codedriver.module.tenant.api.user;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetUserCountBySelectedUserAndTeamApi extends PrivateApiComponentBase {

	@Resource
	private UserMapper userMapper;

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
		Set<String> uuidList = new HashSet<>();
		JSONArray userUuidList = jsonObj.getJSONArray("userUuidList");
		if(CollectionUtils.isNotEmpty(userUuidList)){
			List<String> list = userMapper.checkUserUuidListIsExists(userUuidList.toJavaList(String.class),1);
			if(CollectionUtils.isNotEmpty(list)){
			    uuidList.addAll(list.stream().collect(Collectors.toSet()));
            }
		}
		JSONArray teamUuidList = jsonObj.getJSONArray("teamUuidList");
		if(CollectionUtils.isNotEmpty(teamUuidList)){
            List<String> list = userMapper.getUserUuidListByTeamUuidList(teamUuidList.toJavaList(String.class));
            if(CollectionUtils.isNotEmpty(list)){
                uuidList.addAll(list.stream().collect(Collectors.toSet()));
            }
        }
        result.put("count",uuidList.size());
		return result;
	}
}
