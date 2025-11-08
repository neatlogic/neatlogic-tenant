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
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.heartbeat.dao.mapper.ServerMapper;
import neatlogic.framework.heartbeat.dto.ServerClusterVo;
import neatlogic.framework.heartbeat.dto.ServerCounterVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

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

    @Input({
            @Param(name = "status", type = ApiParamType.ENUM, rule = "startup,stop", desc = "common.status")
    })
    @Output({
            @Param(name = "tbodyList", explode = ServerCounterVo[].class, desc = "common.tbodylist")
    })
    @Description(desc = "nmtas.getserverlistapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String status = paramObj.getString("status");
        List<ServerClusterVo> list = serverMapper.getAllServerList();
        if (StringUtils.isNotBlank(status)) {
            for (int i = list.size() - 1; i >= 0; i--) {
                ServerClusterVo serverClusterVo = list.get(i);
                if (!Objects.equals(serverClusterVo.getStatus(), status)) {
                    list.remove(i);
                }
            }
        }
        JSONObject resultObj = TableResultUtil.getResult(list);
        resultObj.put("currentServerId", Config.SCHEDULE_SERVER_ID);
        return resultObj;
    }

    @Override
    public String getToken() {
        return "server/list";
    }
}
