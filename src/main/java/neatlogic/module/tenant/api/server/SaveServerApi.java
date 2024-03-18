/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

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
