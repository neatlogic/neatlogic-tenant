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

package neatlogic.module.tenant.api.util;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.RequestContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.cache.NeatLogicConcurrentSafeCache;
import neatlogic.framework.exception.core.ApiRuntimeException;
import neatlogic.framework.heartbeat.dao.mapper.ServerMapper;
import neatlogic.framework.heartbeat.dto.ServerClusterVo;
import neatlogic.framework.integration.authentication.enums.AuthenticateType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.HttpRequestUtil;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetNeatLogicConcurrentSafeCacheLockKeyApi extends PrivateApiComponentBase {

    @Resource
    private ServerMapper serverMapper;

    @Override
    public String getName() {
        return "获取Mybaties二级缓存NeatLogicConcurrentSafeCache中lockKey列表";
    }

    @Input({
            @Param(name = "serverId", type = ApiParamType.INTEGER, desc = "服务器ID")
    })
    @Description(desc = "获取Mybaties二级缓存NeatLogicConcurrentSafeCache中lockKey列表")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject resultObj = new JSONObject(new LinkedHashMap<>());
        Integer serverId = paramObj.getInteger("serverId");
        if (serverId == null) {
            serverId = Config.SCHEDULE_SERVER_ID;
        }
        if (Objects.equals(serverId, Config.SCHEDULE_SERVER_ID)) {
            List<String> lockKeyList = NeatLogicConcurrentSafeCache.getAllLockKeyList();
            resultObj.put("lockKeyList", lockKeyList);
            resultObj.put("serverId", serverId);
        } else {
            ServerClusterVo serverClusterVo = serverMapper.getServerByServerId(serverId);
            if (serverClusterVo != null) {
                String host = serverClusterVo.getHost();
                if (StringUtils.isNotBlank(host)) {
                    HttpServletRequest request = RequestContext.get().getRequest();
                    String url = host + request.getRequestURI();
                    HttpRequestUtil httpRequestUtil = HttpRequestUtil.post(url)
                            .setPayload(paramObj.toJSONString())
                            .setAuthType(AuthenticateType.BUILDIN)
                            .setConnectTimeout(5000)
                            .setReadTimeout(5000)
                            .sendRequest();
                    String error = httpRequestUtil.getError();
                    if (StringUtils.isNotBlank(error)) {
                        throw new ApiRuntimeException(error);
                    }
                    JSONObject resultJson = httpRequestUtil.getResultJson();
                    if (MapUtils.isNotEmpty(resultJson)) {
                        String status = resultJson.getString("Status");
                        if (!"OK".equals(status)) {
                            throw new RuntimeException(resultJson.getString("Message"));
                        }
                        resultObj = resultJson.getJSONObject("Return");
                    }
                } else {
                    resultObj.put("message", "serverId为" + serverId + "的应用服务器的`server_status`表中对应数据没有配置host");
                }
            } else {
                resultObj.put("message", "找不到serverId为" + serverId + "的应用服务器");
            }
        }
        return resultObj;
    }

    @Override
    public String getToken() {
        return "util/neatlogicconcurrentsafecache/lockkeylist";
    }
}
