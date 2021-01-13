package codedriver.module.tenant.api.groupsearch;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.restful.core.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

@Deprecated
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class GroupSearchListApi extends PrivateApiComponentBase {
	@Override
	public String getToken() {
		return "groupsearch/list";
	}

	@Override
	public String getName() {
		return "获取组搜索类型接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		
	})
	@Output({
		@Param(name = "value", type=ApiParamType.STRING, desc = "值"),
		@Param(name = "text", type=ApiParamType.STRING, desc = "显示文本")
	})
	@Description(desc = "获取组搜索类型接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONArray jsonArray = new JSONArray();
		for(GroupSearch gs : GroupSearch.values()) {
			JSONObject json = new JSONObject();
			json.put("text", gs.getText());
			json.put("value", gs.getValue());
			jsonArray.add(json);
		}
		return jsonArray;
	}
}
