/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.role;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.role.RoleNotFoundException;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.TableResultUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class RoleUserListApi extends PrivateApiComponentBase  {

	@Autowired
	private RoleMapper roleMapper;

	@Autowired
    private UserMapper userMapper;

	@Override
	public String getToken() {
		return "role/user/list";
	}

	@Override
	public String getName() {
		return "获取角色用户成员列表";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
        @Param(name = "roleUuid", type = ApiParamType.STRING, isRequired = true, desc = "角色uuid"),
        @Param(name = "keyword", type = ApiParamType.STRING, isRequired = false, desc = "关键字"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页展示数量 默认10"),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否分页")
	})
	@Output({
		@Param(name = "userList", explode = UserVo[].class, desc = "角色用户成员列表")
	})
	@Description( desc = "获取角色用户成员列表")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String roleUuid = jsonObj.getString("roleUuid");
		UserVo userVo = JSONObject.toJavaObject(jsonObj, UserVo.class);
		if(roleMapper.checkRoleIsExists(roleUuid) == 0) {
			throw new RoleNotFoundException(roleUuid);
		}
		int rowNom = userMapper.searchUserCount(userVo);
		userVo.setRowNum(rowNom);
		return TableResultUtil.getResult(userMapper.getUserListByRoleUuid(userVo), userVo);
	}

}
