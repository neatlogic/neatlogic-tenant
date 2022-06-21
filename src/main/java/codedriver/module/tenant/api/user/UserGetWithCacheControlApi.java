/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.user;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.init.MaintenanceMode;
import codedriver.framework.common.config.Config;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.CacheControlType;
import codedriver.framework.common.constvalue.SystemUser;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.user.UserNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class UserGetWithCacheControlApi extends PrivateApiComponentBase {

	@Resource
	private UserMapper userMapper;

	@Override
	public String getToken() {
		return "user/cache/get";
	}

	@Override
	public String getName() {
		return "根据用户id查询用户详情接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@CacheControl(cacheControlType = CacheControlType.MAXAGE, maxAge = 30000)
	@Input({@Param(name = "uuid", type = ApiParamType.STRING, desc = "用户uuid")})
	@Output({@Param(name = "Return", explode = UserVo.class, desc = "用户详情")})
	@Description(desc = "根据用户Id查询用户详情，前端会缓存30000秒，后端mybatis二级缓存30秒")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String userUuid = jsonObj.getString("uuid");
		if (StringUtils.isBlank(userUuid)) {
			userUuid = UserContext.get().getUserUuid(true);
		}
		UserVo userVo = null;
		//维护模式下 获取厂商维护人员信息
		if (Config.ENABLE_SUPERADMIN() && Config.SUPERADMIN().equals(UserContext.get().getUserId())) {
			userVo = MaintenanceMode.getMaintenanceUser();
		}else if(Objects.equals(SystemUser.SYSTEM.getUserUuid(),userUuid)){
			userVo = SystemUser.SYSTEM.getUserVo();
		}else {
			userVo = userMapper.getUserSimpleInfoByUuid(userUuid);
		}
		if (userVo == null) {
			throw new UserNotFoundException(userUuid);
		}
		JSONObject userJson = (JSONObject) JSON.toJSON(userVo);// 防止修改cache vo
		//告诉前端是否为维护模式
		userJson.put("isMaintenanceMode", 0);
		if (Config.ENABLE_SUPERADMIN()) {
			userJson.put("isMaintenanceMode", 1);
		}
		return userJson;
	}
}
