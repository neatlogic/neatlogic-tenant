package neatlogic.module.tenant.api.user;

import java.util.List;
import java.util.Set;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.USER_MODIFY;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.restful.core.IValid;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserAuthVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.exception.user.UserIdRepeatException;
import neatlogic.framework.exception.user.UserNotFoundException;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.UuidUtil;

@Service
@Transactional
@AuthAction(action = USER_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class UserSaveApi extends PrivateApiComponentBase {

	@Autowired
	UserMapper userMapper;

	@Autowired
	TeamMapper teamMapper;

	@Override
	public String getToken() {
		return "user/save";
	}

	@Override
	public String getName() {
		return "保存用户接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "uuid",
				type = ApiParamType.STRING,
				desc = "用户uuid", 
				maxLength = 32,
				minLength = 32),
			@Param(name = "userId",
					type = ApiParamType.STRING,
					desc = "用户Id",
					isRequired = true,
                    xss = true),
			@Param(name = "userName",
					type = ApiParamType.STRING,
					desc = "用户姓名",
					isRequired = true,
					xss = true),
			@Param(name = "password",
					type = ApiParamType.STRING,
					desc = "用户密码",
					xss = true),
			@Param(name = "email",
					type = ApiParamType.STRING,
					desc = "用户邮箱",
					isRequired = false,
					xss = true),
			@Param(name = "phone",
					type = ApiParamType.STRING,
					desc = "用户电话",
					isRequired = false,
					xss = true),
			@Param(name = "isActive",
					type = ApiParamType.INTEGER,
					desc = "是否激活",
					isRequired = false),
			@Param(name = "teamUuidList",
					type = ApiParamType.JSONARRAY,
					desc = "组织uuid",
					isRequired = false),
			@Param(name = "roleUuidList",
					type = ApiParamType.JSONARRAY,
					desc = "角色名称",
					isRequired = false),
			@Param(name = "userInfo",
				type = ApiParamType.STRING,
				desc = "其他信息",
				isRequired = false,
				xss = true),
			@Param(name = "vipLevel",
					type = ApiParamType.ENUM,
					desc = "用户等级",
					isRequired = false,
					rule = "0,1,2,3,4,5"),
			@Param(name = "userAuthList",
				type = ApiParamType.JSONOBJECT,
				desc = "权限列表"
			)/*,
			@Param(name = "saveMode",
					type = ApiParamType.ENUM,
					rule = "merge,replace",
					desc = "保存方式，merge：只更新提供的属性，replace：更新全部属性，不提供的置空")*/ })
	@Output({})
	@Description(desc = "保存用户接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		UserVo userVo = new UserVo();		
		userVo.setUserId(jsonObj.getString("userId"));
		userVo.setUserName(jsonObj.getString("userName"));
		userVo.setPassword(jsonObj.getString("password"));
		userVo.setEmail(jsonObj.getString("email"));
		userVo.setPhone(jsonObj.getString("phone"));
		userVo.setIsActive(jsonObj.getInteger("isActive"));
		userVo.setUserInfo(jsonObj.getString("userInfo"));
		userVo.setVipLevel(jsonObj.getInteger("vipLevel"));
		
		String uuid = jsonObj.getString("uuid");
		if(StringUtils.isBlank(uuid)) {
			userVo.setUuid(UuidUtil.randomUuid());
			if(userMapper.checkUserIdIsIsRepeat(userVo) > 0) {
			    throw new UserIdRepeatException(userVo.getUserId());
			}
			userMapper.insertUser(userVo);
			userMapper.insertUserPassword(userVo);
			JSONObject userAuthObj = jsonObj.getJSONObject("userAuthList");
			if(MapUtils.isNotEmpty(userAuthObj)) {
				Set<String> keySet = userAuthObj.keySet();
				for (String key : keySet){
					JSONArray authArray = userAuthObj.getJSONArray(key);
					for (int j = 0; j < authArray.size(); j++){
						UserAuthVo authVo = new UserAuthVo();
						authVo.setAuth(authArray.getString(j));
						authVo.setAuthGroup(key);
						authVo.setUserUuid(userVo.getUuid());
						userMapper.insertUserAuth(authVo);
					}
				}
			}		
		}else {
			if(userMapper.getUserBaseInfoByUuid(uuid) == null) {
				throw new UserNotFoundException(uuid);
			}
			userVo.setUuid(uuid);
			if(userMapper.checkUserIdIsIsRepeat(userVo) > 0) {
                throw new UserIdRepeatException(userVo.getUserId());
            }
			userMapper.updateUser(userVo);
			userMapper.deleteUserRoleByUserUuid(userVo.getUuid());
			userMapper.deleteUserTeamByUserUuid(userVo.getUuid());
			//更新密码
			if (StringUtils.isNotBlank(userVo.getPassword())){
				userMapper.updateUserPasswordActive(userVo.getUuid());
				List<Long> idList = userMapper.getLimitUserPasswordIdList(userVo.getUuid());
				if (CollectionUtils.isNotEmpty(idList)){
					userMapper.deleteUserPasswordByLimit(userVo.getUuid(), idList);
				}
				userMapper.insertUserPassword(userVo);
			}
		}
		
		List<String> teamUuidList = JSON.parseArray(jsonObj.getString("teamUuidList"), String.class);
		if(CollectionUtils.isNotEmpty(teamUuidList)) {
			for(String teamUuid : teamUuidList) {
				userMapper.insertUserTeam(userVo.getUuid(), teamUuid.replaceAll(GroupSearch.TEAM.getValuePlugin(), StringUtils.EMPTY));
			}
		}
		
		List<String> roleUuidList = JSON.parseArray(jsonObj.getString("roleUuidList"), String.class);
		if(CollectionUtils.isNotEmpty(roleUuidList)) {
			for(String roleUuid : roleUuidList) {
				userMapper.insertUserRole(userVo.getUuid(), roleUuid.replaceAll(GroupSearch.ROLE.getValuePlugin(), StringUtils.EMPTY));
			}
		}
		
		return userVo.getUuid();
	}

	public IValid userId(){
		return value -> {
			UserVo userVo = JSON.toJavaObject(value, UserVo.class);
			if(StringUtils.isBlank(userVo.getUuid())){
				userVo.setUuid(UuidUtil.randomUuid());
			}
			if(userMapper.checkUserIdIsIsRepeat(userVo) > 0) {
				return new FieldValidResultVo(new UserIdRepeatException(userVo.getUserId()));
			}
			return new FieldValidResultVo();
		};
	}
}
