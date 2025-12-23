/*Copyright (C) 2023  深圳极向量科技有限公司 All Rights Reserved.

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

package neatlogic.module.tenant.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.dao.cache.UserSessionCache;
import neatlogic.framework.dao.mapper.UserSessionContentMapper;
import neatlogic.framework.dao.mapper.UserSessionMapper;
import neatlogic.framework.dto.UserSessionVo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserSessionServiceImpl implements UserSessionService {

    @Resource
    private UserSessionMapper userSessionMapper;

    @Resource
    private UserSessionContentMapper userSessionContentMapper;

    @Resource
    private ServerService serverService;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public JSONObject clearUserSessionCache(String userUuid) {
        return clearUserSessionCache(userUuid, 0, new JSONArray());
    }

    @Override
    public JSONObject deleteUserSessionAndCache(String userUuid) {
        userSessionMapper.deleteUserSessionByUserUuid(userUuid);
        return clearUserSessionCache(userUuid, 0, new JSONArray());
    }

    @Override
    public JSONObject clearUserSessionCache(String userUuid, int isPassive, JSONArray removeTokenList) {
        List<UserSessionVo> userSessionVos = userSessionMapper.getUserSessionByUuid(userUuid);
        if (CollectionUtils.isNotEmpty(userSessionVos)) {
            for (UserSessionVo userSessionVo : userSessionVos) {
                JSONObject userSessionJson = new JSONObject();
                userSessionJson.put("tokenHash", userSessionVo.getTokenHash());
                userSessionJson.put("authInfoHash", userSessionVo.getAuthInfoHash());
                if (StringUtils.isNotBlank(userSessionVo.getAuthInfoHash())) {
                    String authInfo = userSessionContentMapper.getUserSessionContentByHash(userSessionVo.getAuthInfoHash());
                    String token = userSessionContentMapper.getUserSessionContentByHash(userSessionVo.getTokenHash());
                    userSessionJson.put("token", token);
                    userSessionJson.put("authInfo", authInfo);
                }
                removeTokenList.add(userSessionJson);
                UserSessionCache.removeItem(userSessionVo.getTokenHash());
            }
        }
        JSONObject result = new JSONObject();
        //清除其它节点的用户信息缓存
        if (isPassive == 0) {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("isPassive", 1);
            jsonObj.put("userUuid", userUuid);
            result.put("resultArray", serverService.postOtherServersApi(jsonObj, Config.SCHEDULE_SERVER_ID, "/user/session/cache/clear"));
        }
        result.put("serverId", Config.SCHEDULE_SERVER_ID);
        result.put("removeTokenList", removeTokenList);
        return result;
    }


}
