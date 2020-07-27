package codedriver.module.tenant.api.role;

import java.util.List;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class RoleSearchApi extends ApiComponentBase {

	@Autowired
	private RoleMapper roleMapper;

	@Override
	public String getToken() {
		return "role/search";
	}

	@Override
	public String getName() {
		return "角色查询接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "keyword",
					type = ApiParamType.STRING,
					desc = "关键字，匹配名称或说明", xss = true),
			@Param(name = "authGroup",
					type = ApiParamType.STRING,
					desc = "权限模块"),
			@Param(name = "auth",
					type = ApiParamType.STRING,
					desc = "权限"),
			@Param(name = "needPage",
					type = ApiParamType.BOOLEAN,
					desc = "是否需要分页，默认true"),
			@Param(name = "pageSize",
					type = ApiParamType.INTEGER,
					desc = "每页条目"),
			@Param(name = "currentPage",
					type = ApiParamType.INTEGER,
					desc = "当前页") })
	@Output({
			@Param(name = "tbodyList",
					explode = RoleVo[].class,
					desc = "table数据列表"),
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
	@Description(desc = "角色查询接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject returnObj = new JSONObject();
		RoleVo roleVo = JSON.toJavaObject(jsonObj, RoleVo.class);
		if(roleVo.getNeedPage()) {
			int rowNum = roleMapper.searchRoleCount(roleVo);
			returnObj.put("pageSize", roleVo.getPageSize());
			returnObj.put("currentPage", roleVo.getCurrentPage());
			returnObj.put("rowNum", rowNum);
			returnObj.put("pageCount", PageUtil.getPageCount(rowNum, roleVo.getPageSize()));
		}
		List<RoleVo> roleList =  roleMapper.searchRole(roleVo);
		returnObj.put("tbodyList", roleList);
		return returnObj;
	}
}
