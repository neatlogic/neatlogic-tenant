package codedriver.module.tenant.api.role;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.restful.core.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class RoleSearchForSelectApi extends PrivateApiComponentBase {

	@Autowired
	private RoleMapper roleMapper;

	@Override
	public String getToken() {
		return "role/search/forselect";
	}

	@Override
	public String getName() {
		return "查询角色_下拉";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "keyword",
					type = ApiParamType.STRING,
					desc = "关键字，匹配名称或说明", xss = true),
			@Param(name = "needPage",
					type = ApiParamType.BOOLEAN,
					desc = "是否需要分页，默认true"),
			@Param(name = "pageSize",
					type = ApiParamType.INTEGER,
					desc = "每页条目"),
			@Param(name = "currentPage",
					type = ApiParamType.INTEGER,
					desc = "当前页")})
	@Output({
			@Param(name = "list",
					type = ApiParamType.JSONARRAY,
					explode = ValueTextVo[].class,
					desc = "选项列表"),
			@Param(name = "pageSize",
					type = ApiParamType.INTEGER,
					desc = "每页数据条目"),
			@Param(name = "currentPage",
					type = ApiParamType.INTEGER,
					desc = "当前页"),
			@Param(name = "rowNum",
					type = ApiParamType.INTEGER,
					desc = "返回条目总数"),
			@Param(name = "pageCount",
					type = ApiParamType.INTEGER,
					desc = "总页数")})
	@Description(desc = "查询角色_下拉")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		
		JSONObject resultObj = new JSONObject();
		RoleVo roleVo = JSON.toJavaObject(jsonObj, RoleVo.class);
		if(roleVo.getNeedPage()) {
			int rowNum = roleMapper.searchRoleCount(roleVo);
			resultObj.put("pageSize", roleVo.getPageSize());
			resultObj.put("currentPage", roleVo.getCurrentPage());
			resultObj.put("rowNum", rowNum);
			resultObj.put("pageCount", PageUtil.getPageCount(rowNum, roleVo.getPageSize()));
		}
		resultObj.put("list", roleMapper.searchRoleForSelect(roleVo));
		return resultObj;
	}
}
