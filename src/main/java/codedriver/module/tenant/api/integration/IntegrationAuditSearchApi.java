package codedriver.module.tenant.api.integration;

import java.util.List;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.FRAMEWORK_BASE;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationAuditVo;

@Service
@AuthAction(action = FRAMEWORK_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class IntegrationAuditSearchApi extends PrivateApiComponentBase {

	@Autowired
	private IntegrationMapper integrationMapper;

	@Override
	public String getToken() {
		return "integration/audit/search";
	}

	@Override
	public String getName() {
		return "集成调用审计查询接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "integrationUuid", type = ApiParamType.STRING, desc = "集成设置uuid"), @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"), @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数量") })
	@Output({ @Param(explode = BasePageVo.class), @Param(name = "tbodyList", explode = IntegrationAuditVo[].class) })
	@Description(desc = "集成调用审计查询接口")
	@Override
	public Object myDoService(JSONObject jsonObj) {
		IntegrationAuditVo integrationAuditVo = JSONObject.toJavaObject(jsonObj, IntegrationAuditVo.class);
		List<IntegrationAuditVo> integrationAuditList = integrationMapper.searchIntegrationAudit(integrationAuditVo);
		int rowNum = integrationMapper.searchIntegrationAuditCount(integrationAuditVo);
		JSONObject returnObj = new JSONObject();
		returnObj.put("pageSize", integrationAuditVo.getPageSize());
		returnObj.put("currentPage", integrationAuditVo.getCurrentPage());
		returnObj.put("rowNum", rowNum);
		returnObj.put("pageCount", PageUtil.getPageCount(rowNum, integrationAuditVo.getPageSize()));
		returnObj.put("tbodyList", integrationAuditList);
		return returnObj;
	}
}
