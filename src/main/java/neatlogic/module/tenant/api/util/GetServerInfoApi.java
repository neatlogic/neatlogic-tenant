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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.service.ServerService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetServerInfoApi extends PrivateApiComponentBase {

    @Resource
    private ServletContext servletContext;
    @Resource
    ServerService serverService;


    @Override
    public String getName() {
        return "nmtau.getserverinfoapi.getname";
    }

    @Input({
            @Param(name = "serverId", type = ApiParamType.INTEGER, desc = "term.framework.serverid")
    })
    @Description(desc = "nmtau.getserverinfoapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject resultObj = new JSONObject(new LinkedHashMap<>());
        Integer serverId = paramObj.getInteger("serverId");
        if (serverId == null) {
            serverId = Config.SCHEDULE_SERVER_ID;
        }
        if (Objects.equals(serverId, Config.SCHEDULE_SERVER_ID)) {
            resultObj.put("Server.服务器版本", servletContext.getServerInfo());
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
            {
                Properties prop = System.getProperties();
                Map<String, Object> map = new LinkedHashMap<>();
                for (Map.Entry<Object, Object> entry : prop.entrySet()) {
                    map.put(entry.getKey().toString(), entry.getValue());
                }
                Map<String, Object> sortedMap = map.entrySet().stream().sorted(Map.Entry.comparingByKey())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
                resultObj.put("Java虚拟机的系统属性", sortedMap);
            }
            {
                Map<String, String> map = System.getenv();
                Map<String, Object> sortedMap = map.entrySet().stream().sorted(Map.Entry.comparingByKey())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
                resultObj.put("操作系统的环境变量", sortedMap);
            }
            resultObj.put("serverId", serverId);
        } else {
            resultObj = serverService.postOtherServerApi(paramObj, serverId);
        }
        return resultObj;
    }

    @Override
    public String getToken() {
        return "util/serverinfo/get";
    }
}
