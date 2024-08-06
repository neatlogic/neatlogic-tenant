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

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.cache.UserSessionCache;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dao.mapper.UserSessionMapper;
import neatlogic.framework.dto.UserSessionVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.exception.user.UserNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ClearUserSessionCacheApi extends PrivateApiComponentBase {
    @Resource
    UserMapper userMapper;

    @Resource
    UserSessionMapper userSessionMapper;

    @Override
    public String getToken() {
        return "/user/session/cache/clear";
    }

    @Override
    public String getName() {
        return "清楚用户会话缓存";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "userUuid", type = ApiParamType.STRING, desc = "common.useruuid"),
            @Param(name = "useId", type = ApiParamType.STRING, desc = "common.userid")
    })
    @Output({})
    @Description(desc = "清楚用户会话缓存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        UserVo userVo = null;
        String userUuid = null;
        if (jsonObj.containsKey("userUuid")) {
            userUuid = jsonObj.getString("userUuid");
            userVo = userMapper.getUserByUuid(userUuid);
            if (userVo == null) {
                throw new UserNotFoundException(userUuid);
            }
        } else if (jsonObj.containsKey("userId")) {
            String userId = jsonObj.getString("userId");
            userVo = userMapper.getUserByUserId(userId);
            if (userVo == null) {
                throw new UserNotFoundException(userId);
            }
            userUuid = userVo.getUuid();
        }
        if (StringUtils.isBlank(userUuid)) {
            userUuid = UserContext.get().getUserUuid(true);
            UserSessionCache.removeItem(UserContext.get().getTokenHash());
        }
        List<UserSessionVo> userSessionVos = userSessionMapper.getUserSessionByUuid(userUuid);
        if (CollectionUtils.isNotEmpty(userSessionVos)) {
            for (UserSessionVo userSessionVo : userSessionVos) {
                UserSessionCache.removeItem(userSessionVo.getTokenHash());
            }
        }
        return null;
    }
}
