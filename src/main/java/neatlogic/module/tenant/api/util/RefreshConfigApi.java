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
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.config.LocalConfig;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class RefreshConfigApi extends PrivateApiComponentBase {
    @Resource
    ServerService serverService;

    @Override
    public String getName() {
        return "刷新config.properties文件配置变量值";
    }

    @Input({
            @Param(name = "serverId", type = ApiParamType.INTEGER, desc = "服务器ID")
    })
    @Description(desc = "刷新config.properties文件配置变量值")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        Integer serverId = paramObj.getInteger("serverId");
        if (serverId == null) {
            serverId = Config.SCHEDULE_SERVER_ID;
        }
        if (Objects.equals(serverId, Config.SCHEDULE_SERVER_ID)) {
            Properties prop = new Properties();
            LocalConfig.loadProperties(prop);
            Config.loadLocalOrNacosProperties(prop);
            if (Objects.equals(LocalConfig.ConfigSource.NACOS,LocalConfig.getConfigSource())) {
                resultObj.put("数据来源", "Nacos");
            } else {
                resultObj.put("数据来源", "config.properties");
            }
            Map<String, Object> map = new LinkedHashMap<>();
            for (Map.Entry<Object, Object> entry : prop.entrySet()) {
                map.put(entry.getKey().toString(), entry.getValue());
            }
            Map<String, Object> sortedMap = map.entrySet().stream().sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
            resultObj.put("config", sortedMap);
            resultObj.put("serverId", serverId);
        } else {
            return serverService.postOtherServerApi(paramObj, serverId);
        }
        return resultObj;
    }

    @Override
    public String getToken() {
        return "util/config/refresh";
    }
}
