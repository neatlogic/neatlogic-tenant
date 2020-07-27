package codedriver.module.tenant.api.apiaudit;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.restful.dto.ApiAuditVo;
import codedriver.module.tenant.service.apiaudit.ApiAuditService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 操作审计查询接口
 */

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ApiAuditSearchApi extends ApiComponentBase {

	@Autowired
	private ApiAuditService apiAuditService;

	@Override
	public String getToken() {
		return "apiaudit/search";
	}

	@Override
	public String getName() {
		return "查询操作审计";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "moduleGroup", type = ApiParamType.STRING, desc = "API所属模块"),
		@Param(name = "funcId", type = ApiParamType.STRING, desc = "API所属功能"),
		@Param(name = "userUuid", type = ApiParamType.STRING, desc = "访问者UUID"),
		@Param(name = "operationType", type = ApiParamType.STRING, desc = "操作类型"),
		@Param(name = "timeRange", type = ApiParamType.INTEGER, desc="时间跨度"),
		@Param(name = "timeUnit", type = ApiParamType.STRING, desc="时间跨度单位(day|month)"),
		@Param(name = "orderType", type = ApiParamType.STRING, desc="排序类型(asc|desc)"),
		@Param(name = "startTime", type = ApiParamType.LONG, desc="开始时间"),
		@Param(name = "endTime", type = ApiParamType.LONG, desc="结束时间"),
		@Param(name = "keyword", type = ApiParamType.STRING, desc="搜索关键词")
	})
	@Output({
	})
	@Description(desc = "查询操作审计")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ApiAuditVo apiAuditVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ApiAuditVo>() {});

		//筛选出符合条件的所有记录
		List<ApiAuditVo> apiAuditVoList = apiAuditService.searchApiAuditVo(apiAuditVo);

		if(CollectionUtils.isNotEmpty(apiAuditVoList)){
			int rowNum = apiAuditVoList.size();
			apiAuditVo.setRowNum(rowNum);
			apiAuditVo.setPageCount(PageUtil.getPageCount(rowNum, apiAuditVo.getPageSize()));
		}
		JSONObject returnObj = new JSONObject();
		returnObj.put("pageSize", apiAuditVo.getPageSize());
		returnObj.put("currentPage", apiAuditVo.getCurrentPage());
		returnObj.put("rowNum", apiAuditVo.getRowNum());
		returnObj.put("pageCount", apiAuditVo.getPageCount());
		returnObj.put("tbodyList", apiAuditVoList);

		return returnObj;
	}
}
