package codedriver.module.tenant.api.notify;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.ApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyPolicySearchForSelectApi extends ApiComponentBase {
	
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
		@Param(explode=BasePageVo.class),
		@Param(name = "tbodyList", explode = NotifyPolicyVo[].class, desc = "通知策略列表")
	})
	@Description(desc = "查询通知策略管理列表_下拉框")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject resultObj = new JSONObject();
		NotifyPolicyVo notifyPolicyVo = JSON.toJavaObject(jsonObj, NotifyPolicyVo.class);
		List<ValueTextVo> tbodyList = notifyMapper.getNotifyPolicyListForSelect(notifyPolicyVo);

		resultObj.put("tbodyList", tbodyList);
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
