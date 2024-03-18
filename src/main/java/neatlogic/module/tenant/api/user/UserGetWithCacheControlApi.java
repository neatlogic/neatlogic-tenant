/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tenant.api.user;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.init.MaintenanceMode;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.CacheControlType;
import neatlogic.framework.common.constvalue.SystemUser;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
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
		if (Config.ENABLE_MAINTENANCE() && Config.MAINTENANCE().equals(UserContext.get().getUserId())) {
			userVo = MaintenanceMode.getMaintenanceUser();
		}else if(Objects.equals(SystemUser.SYSTEM.getUserUuid(),userUuid)){
			userVo = SystemUser.SYSTEM.getUserVo(false);
		}else {
			userVo = userMapper.getUserSimpleInfoByUuid(userUuid);
		}
		if (userVo == null) {
			return null;
		}
		JSONObject userJson = JSONObject.parseObject(JSONObject.toJSONString(userVo));
		//告诉前端是否为维护模式
		userJson.put("isMaintenanceMode", 0);
		if (Config.ENABLE_MAINTENANCE()) {
			userJson.put("isMaintenanceMode", 1);
		}
		return userJson;
	}
}
