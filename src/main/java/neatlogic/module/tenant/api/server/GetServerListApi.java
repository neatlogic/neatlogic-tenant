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
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.heartbeat.dao.mapper.ServerMapper;
import neatlogic.framework.heartbeat.dto.ServerClusterVo;
import neatlogic.framework.heartbeat.dto.ServerCounterVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@AuthAction(action = ADMIN.class)
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetServerListApi extends PrivateApiComponentBase {

    @Resource
    private ServerMapper serverMapper;

    @Override
    public String getName() {
        return "nmtas.getserverlistapi.getname";
    }

    @Input({})
    @Output({
            @Param(name = "tbodyList", explode = ServerCounterVo[].class, desc = "common.tbodylist")
    })
    @Description(desc = "nmtas.getserverlistapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        TenantContext.get().setUseDefaultDatasource(true);
        List<ServerClusterVo> list = serverMapper.getAllServerList();
        TenantContext.get().setUseDefaultDatasource(false);
        return TableResultUtil.getResult(list);
    }

    @Override
    public String getToken() {
        return "server/list";
    }
}
