/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.api.user;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.cache.UserSessionCache;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.exception.LoadBalanceException;
import neatlogic.framework.exception.user.UserNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.service.UserSessionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ClearUserSessionCacheApi extends PrivateApiComponentBase {
    @Resource
    UserMapper userMapper;

    @Resource
    UserSessionService userSessionService;

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
            @Param(name = "serverId", type = ApiParamType.INTEGER, isRequired = true, desc = "term.framework.serverid"),
            @Param(name = "isPassive", type = ApiParamType.INTEGER, desc = "是否被动清理，0：否，1：是，默认 0"),
            @Param(name = "userUuid", type = ApiParamType.STRING, desc = "common.useruuid"),
            @Param(name = "useId", type = ApiParamType.STRING, desc = "common.userid")
    })
    @Output({})
    @Description(desc = "清楚用户会话缓存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        UserVo userVo = null;
        String userUuid = null;
        int serverId = jsonObj.getIntValue("serverId");
        int isPassive = jsonObj.getIntValue("isPassive");

        if (isPassive == 0 && serverId != Config.SCHEDULE_SERVER_ID) {
            throw new LoadBalanceException(serverId, Config.SCHEDULE_SERVER_ID);
        }

        JSONArray removeTokenList = new JSONArray();
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
            removeTokenList.add(UserContext.get().getTokenHash());
            UserSessionCache.removeItem(UserContext.get().getTokenHash());
        }
        return userSessionService.clearUserSessionCache(userUuid, isPassive, removeTokenList);
    }
}
