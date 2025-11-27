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
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.cache.UserSessionCache;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dao.mapper.UserSessionMapper;
import neatlogic.framework.dto.UserSessionVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.service.UserSessionService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ClearUserSessionCacheApi extends PrivateApiComponentBase {
    @Resource
    UserMapper userMapper;

    @Resource
    UserSessionMapper userSessionMapper;

    @Resource
    UserSessionService userSessionService;

    @Override
    public String getToken() {
        return "/user/session/cache/clear";
    }

    @Override
    public String getName() {
        return "清除用户会话缓存";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "userIdList", type = ApiParamType.STRING, desc = "用户id或uuid列表"),
            @Param(name = "tokenHashList", type = ApiParamType.STRING, desc = "tokenHash列表"),
    })
    @Output({})
    @Description(desc = "清除用户会话缓存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        //如果是存在tokenHashList，说明是被动需要清除用户会话
        if (jsonObj.containsKey("tokenHashList")) {
            List<String> tokenHashList = JSONArray.parseArray(jsonObj.getString("tokenHashList"), String.class);
            for (String tokenHash : tokenHashList) {
                UserSessionCache.removeItem(tokenHash);
            }
        } else {
            List<String> userUuidList = new ArrayList<>();
            List<String> removeTokenList = new ArrayList<>();
            if (jsonObj.containsKey("userIdList")) {
                List<String> userIdList = JSONArray.parseArray(jsonObj.getString("userUuidList"), String.class);
                List<UserVo> userList = userMapper.getUserByUserIdListOrUuidList(userIdList);
                if (CollectionUtils.isNotEmpty(userList)) {
                    userUuidList = userList.stream().map(UserVo::getUuid).toList();
                }
            } else {
                userUuidList.add(UserContext.get().getUserUuid(true));
                removeTokenList.add(UserContext.get().getTokenHash());
            }

            List<UserSessionVo> userSessionVos = userSessionMapper.getUserSessionByUuidList(userUuidList);
            if (CollectionUtils.isNotEmpty(userSessionVos)) {
                for (UserSessionVo userSessionVo : userSessionVos) {
                    UserSessionCache.removeItem(userSessionVo.getTokenHash());
                    removeTokenList.add(userSessionVo.getTokenHash());
                }
            }
            //调取其它节点的接口删除UserSessionCache
            userSessionService.deleteOtherClusterUserSessionByTokenList(removeTokenList);
        }
        return null;
    }
}
