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

package neatlogic.module.tenant.api.server;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.heartbeat.dao.mapper.ServerMapper;
import neatlogic.framework.heartbeat.dto.ServerClusterVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.RegexUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class SaveServerApi extends PrivateApiComponentBase {


    @Resource
    private ServerMapper serverMapper;

    @Override
    public String getName() {
        return "nmtas.saveserverapi.getname";
    }

    @Input({
            @Param(name = "serverId", type = ApiParamType.LONG, isRequired = true, desc = "term.framework.serverid"),
            @Param(name = "host", type = ApiParamType.REGEX, rule = RegexUtils.SERVER_HOST, isRequired = true, desc = "term.framework.serveripport", help = "http(s)://ip:port")
    })
    @Description(desc = "nmtas.saveserverapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        ServerClusterVo serverVo = paramObj.toJavaObject(ServerClusterVo.class);
        ServerClusterVo oldServerClusterVo = serverMapper.getServerByServerId(serverVo.getServerId());
        if (oldServerClusterVo == null) {
            return null;
        }
        if (!Objects.equals(oldServerClusterVo.getHost(), serverVo.getHost())) {
            serverVo.setFcu(UserContext.get().getUserUuid());
            serverVo.setLcu(UserContext.get().getUserUuid());
            serverMapper.updateServerHostByServerId(serverVo);
        }
        return null;
    }

    @Override
    public String getToken() {
        return "server/save";
    }
}
