package neatlogic.module.tenant.api.role;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ROLE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dto.RoleUserVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.service.UserService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@AuthAction(action = ROLE_MODIFY.class)
@Service
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
public class RoleDeleteApi extends PrivateApiComponentBase {

	@Resource
	private RoleMapper roleMapper;

	@Resource
	UserService userService;

	@Override
	public String getToken() {
		return "role/delete";
	}

	@Override
	public String getName() {
		return "nmtar.roledeleteapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "uuidList",
					type = ApiParamType.JSONARRAY,
					desc = "common.roleuuidlist",
					isRequired = true) })
	@Description(desc = "nmtar.roledeleteapi.getname")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		List<String> uuidList = JSON.parseArray(jsonObj.getString("uuidList"), String.class);
		List<String> userUuidList = null;
		if(CollectionUtils.isNotEmpty(uuidList)) {
			List<RoleUserVo> roleUserList = roleMapper.getRoleUserListByRoleUuidList(uuidList);
			if(CollectionUtils.isNotEmpty(roleUserList)){
				userUuidList = roleUserList.stream().map(RoleUserVo::getUserUuid).collect(Collectors.toList());
			}
			for (String uuid : uuidList) {
				roleMapper.deleteMenuRoleByRoleUuid(uuid);
				roleMapper.deleteTeamRoleByRoleUuid(uuid);
				roleMapper.deleteRoleUser(new RoleUserVo(uuid));
//			roleMapper.deleteRoleByUuid(uuid);
				roleMapper.updateRoleIsDeletedByUuid(uuid);
				roleMapper.deleteRoleAuthByRoleUuid(uuid);
			}

//			if(CollectionUtils.isNotEmpty(userUuidList)){
//				for (String userUuid : userUuidList){
//					userService.updateUserCacheAndSessionByUserUuid(userUuid);
//				}
//			}
		}
		return null;
	}
}
