/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
		if (Config.ENABLE_SUPERADMIN() && Config.SUPERADMIN().equals(UserContext.get().getUserId())) {
			userVo = MaintenanceMode.getMaintenanceUser();
		}else if(Objects.equals(SystemUser.SYSTEM.getUserUuid(),userUuid)){
			userVo = SystemUser.SYSTEM.getUserVo();
		}else {
			userVo = userMapper.getUserSimpleInfoByUuid(userUuid);
		}
		if (userVo == null) {
			return null;
		}
		JSONObject userJson = JSONObject.parseObject(JSONObject.toJSONString(userVo));
		//告诉前端是否为维护模式
		userJson.put("isMaintenanceMode", 0);
		if (Config.ENABLE_SUPERADMIN()) {
			userJson.put("isMaintenanceMode", 1);
		}
		return userJson;
	}
}
