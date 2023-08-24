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
