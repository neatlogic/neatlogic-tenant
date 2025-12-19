/*
 * Copyright (C) 2025  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neatlogic.module.tenant.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.RequestContext;
import neatlogic.framework.exception.core.ApiRuntimeException;
import neatlogic.framework.heartbeat.dao.mapper.ServerMapper;
import neatlogic.framework.heartbeat.dto.ServerClusterVo;
import neatlogic.framework.integration.authentication.enums.AuthenticateType;
import neatlogic.framework.util.$;
import neatlogic.framework.util.HttpRequestUtil;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service
public class ServerServiceImpl implements ServerService {
    @Resource
    ServerMapper serverMapper;

    @Override
    public JSONObject postOtherServerApi(JSONObject paramObj, Integer serverId) {
        JSONObject resultObj = new JSONObject();
        String errorMessage = null;
        ServerClusterVo serverClusterVo = serverMapper.getServerLockByServerId(serverId);
        if (serverClusterVo != null) {
            resultObj.putAll(postOtherServerApi(paramObj, serverClusterVo));
        } else {
            errorMessage = $.t("nmts.serverserviceimpl.postotherserverapi.notfound", serverId);
        }
        resultObj.put("message", errorMessage);
        return resultObj;
    }

    @Override
    public JSONObject postOtherServerApi(JSONObject paramObj, ServerClusterVo serverClusterVo) {
        return postOtherServerApi(paramObj, serverClusterVo, null);
    }

    @Override
    public JSONObject postOtherServerApi(JSONObject paramObj, ServerClusterVo serverClusterVo, String uri) {
        JSONObject resultObj = new JSONObject();
        String errorMessage = null;
        String url;
        String host = serverClusterVo.getHost();
        if (StringUtils.isNotBlank(host) || StringUtils.isNotBlank(uri)) {
            HttpServletRequest request = RequestContext.get().getRequest();
            if (StringUtils.isBlank(uri)) {
                url = host + request.getRequestURI();
            } else {
                url = host + uri;
            }
            HttpRequestUtil httpRequestUtil = HttpRequestUtil.post(url).setPayload(paramObj.toJSONString()).setAuthType(AuthenticateType.BUILDIN).setConnectTimeout(5000).setReadTimeout(5000).sendRequest();
            String error = httpRequestUtil.getError();
            if (StringUtils.isNotBlank(error)) {
                throw new ApiRuntimeException(error);
            }
            JSONObject resultJson = httpRequestUtil.getResultJson();
            if (MapUtils.isNotEmpty(resultJson)) {
                String status = resultJson.getString("Status");
                if (!"OK".equals(status)) {
                    errorMessage = "serverId:“" + serverClusterVo.getServerId() + "”，" + resultJson.getString("Message");
                }
                resultObj = resultJson.getJSONObject("Return");
            }
        } else {
            errorMessage = $.t("nmts.serverserviceimpl.postotherserverapi.nothost", serverClusterVo.getServerId());
        }
        resultObj.put("message", errorMessage);
        return resultObj;
    }

    @Override
    public JSONArray postOtherServersApi(JSONObject paramObj, int serverId) {
        JSONArray resultArray = new JSONArray();
        List<ServerClusterVo> serverList = serverMapper.getOtherStartUpServerByServerId(serverId);
        for (ServerClusterVo serverClusterVo : serverList) {
            resultArray.add(postOtherServerApi(paramObj, serverClusterVo));
        }
        return resultArray;
    }

    @Override
    public JSONArray postOtherServersApi(JSONObject paramObj, int serverId, String uri) {
        JSONArray resultArray = new JSONArray();
        List<ServerClusterVo> serverList = serverMapper.getOtherStartUpServerByServerId(serverId);
        for (ServerClusterVo serverClusterVo : serverList) {
            resultArray.add(postOtherServerApi(paramObj, serverClusterVo, uri));
        }
        return resultArray;
    }

    @Override
    public String downloadOtherServerApi(JSONObject paramObj, int serverId, ServletOutputStream os) {
        String errorMessage;
        ServerClusterVo serverClusterVo = serverMapper.getServerLockByServerId(serverId);
        if (serverClusterVo != null) {
            errorMessage = downloadOtherServerApi(paramObj, serverClusterVo, os);
        } else {
            errorMessage = $.t("nmts.serverserviceimpl.postotherserverapi.notfound", serverId);
        }
        return errorMessage;
    }

    @Override
    public String downloadOtherServerApi(JSONObject paramObj, ServerClusterVo serverClusterVo, ServletOutputStream os) {
        String errorMessage = null;
        String host = serverClusterVo.getHost();
        if (StringUtils.isNotBlank(host)) {
            HttpServletRequest request = RequestContext.get().getRequest();
            String url = host + request.getRequestURI();
            HttpRequestUtil httpRequestUtil = HttpRequestUtil.download(url, "POST", os).setPayload(paramObj.toJSONString()).setAuthType(AuthenticateType.BUILDIN).setConnectTimeout(5000).setReadTimeout(5000).sendRequest();
            String error = httpRequestUtil.getError();
            if (StringUtils.isNotBlank(error)) {
                throw new ApiRuntimeException(error);
            }
            JSONObject resultJson = httpRequestUtil.getResultJson();
            if (MapUtils.isNotEmpty(resultJson)) {
                String status = resultJson.getString("Status");
                if (!"OK".equals(status)) {
                    errorMessage = "serverId:“" + serverClusterVo.getServerId() + "”，" + resultJson.getString("Message");
                }
            }
        } else {
            errorMessage = $.t("nmts.serverserviceimpl.postotherserverapi.nothost", serverClusterVo.getServerId());
        }
        return errorMessage;
    }


}
