package codedriver.module.tenant.api.notify;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.FRAMEWORK_BASE;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AuthAction(action = FRAMEWORK_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyPolicySearchForSelectApi extends PrivateApiComponentBase {
	
	@Autowired
	private NotifyMapper notifyMapper;

	@Override
	public String getToken() {
		return "notify/policy/search/forselect";
	}

	@Override
	public String getName() {
		return "查询通知策略管理列表_下拉框";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字搜索"),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否分页"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条数"),
		@Param(name = "handler", type = ApiParamType.STRING, isRequired = true, desc = "通知策略处理器")
	})
	@Output({
		@Param(name = "list", explode = ValueTextVo[].class, desc = "通知策略列表")
	})
	@Description(desc = "查询通知策略管理列表_下拉框")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject resultObj = new JSONObject();
		NotifyPolicyVo notifyPolicyVo = JSON.toJavaObject(jsonObj, NotifyPolicyVo.class);
		List<ValueTextVo> tbodyList = notifyMapper.getNotifyPolicyListForSelect(notifyPolicyVo);

		resultObj.put("list", tbodyList);
		if(notifyPolicyVo.getNeedPage()) {
			int rowNum = notifyMapper.getNotifyPolicyCount(notifyPolicyVo);
			resultObj.put("currentPage", notifyPolicyVo.getCurrentPage());
			resultObj.put("pageSize", notifyPolicyVo.getPageSize());
			resultObj.put("pageCount", PageUtil.getPageCount(rowNum, notifyPolicyVo.getPageSize()));
			resultObj.put("rowNum", rowNum);
		}
		return resultObj;
	}

}
