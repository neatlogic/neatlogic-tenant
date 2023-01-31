/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.tenant.api.groupsearch;

import neatlogic.framework.auth.core.AuthAction;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

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
