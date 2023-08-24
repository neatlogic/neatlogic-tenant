/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.server;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.heartbeat.dao.mapper.ServerMapper;
import neatlogic.framework.heartbeat.dto.ServerClusterVo;
import neatlogic.framework.restful.annotation.*;
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
    @Output({})
    @Description(desc = "nmtas.saveserverapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        TenantContext.get().setUseDefaultDatasource(true);
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
        TenantContext.get().setUseDefaultDatasource(false);
        return null;
    }

    @Override
    public String getToken() {
        return "server/save";
    }
}
