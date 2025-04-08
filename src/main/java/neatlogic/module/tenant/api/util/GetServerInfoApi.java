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

package neatlogic.module.tenant.api.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.RequestContext;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.ApiParamType;
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
import org.apache.catalina.util.ServerInfo;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.management.ManagementFactory;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetServerInfoApi extends PrivateApiComponentBase {

    @Resource
    private ServerMapper serverMapper;

    @Override
    public String getName() {
        return "获取服务器信息";
    }

    @Input({
            @Param(name = "serverId", type = ApiParamType.INTEGER, desc = "服务器ID")
    })
    @Description(desc = "获取服务器信息")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject resultObj = new JSONObject(new LinkedHashMap<>());
        Integer serverId = paramObj.getInteger("serverId");
        if (serverId == null) {
            serverId = Config.SCHEDULE_SERVER_ID;
        }
        if (Objects.equals(serverId, Config.SCHEDULE_SERVER_ID)) {
            resultObj.put("Server.服务器版本", ServerInfo.getServerInfo());
            resultObj.put("服务器构建", ServerInfo.getServerBuilt());
            resultObj.put("服务器版本号", ServerInfo.getServerNumber());
            resultObj.put("操作系统名称", System.getProperty("os.name"));
            resultObj.put("OS.版本", System.getProperty("os.version"));
            resultObj.put("架构", System.getProperty("os.arch"));
            resultObj.put("Java 环境变量", System.getProperty("java.home"));
            resultObj.put("Java虚拟机版本", System.getProperty("java.runtime.version"));
            resultObj.put("JVM.供应商", System.getProperty("java.vm.vendor"));
            resultObj.put("CATALINA_BASE", System.getProperty("catalina.base"));
            resultObj.put("CATALINA_HOME", System.getProperty("catalina.home"));
            List<String> args = ManagementFactory.getRuntimeMXBean().getInputArguments();
            JSONArray array = new JSONArray();
            array.addAll(args);
            resultObj.put("命令行参数", array);
            resultObj.put("serverId", serverId);
        } else {
            String host = null;
            TenantContext.get().setUseMasterDatabase(true);
            ServerClusterVo serverClusterVo = serverMapper.getServerByServerId(serverId);
            if (serverClusterVo != null) {
                host = serverClusterVo.getHost();
            }
            TenantContext.get().setUseMasterDatabase(false);
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
            }
        }
        return resultObj;
    }

    @Override
    public String getToken() {
        return "util/serverinfo/get";
    }
}
