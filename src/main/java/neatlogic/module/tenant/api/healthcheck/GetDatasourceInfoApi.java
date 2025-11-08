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

package neatlogic.module.tenant.api.healthcheck;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.dto.healthcheck.DataSourceInfoVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.store.mysql.DatasourceManager;
import neatlogic.framework.store.mysql.NeatLogicBasicDataSource;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetDatasourceInfoApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "/healthcheck/datasource";
    }

    @Override
    public String getName() {
        return "获取数据源信息";
    }

    @Override
    public String getConfig() {
        return null;
    }


    @Output({
            @Param(explode = DataSourceInfoVo.class),
    })
    @Description(desc = "获取数据源信息接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        NeatLogicBasicDataSource datasource = DatasourceManager.getDatasource();
        DataSourceInfoVo dataSourceInfoVo = new DataSourceInfoVo();
        dataSourceInfoVo.setPoolName(datasource.getPoolName());
        if (datasource.getHikariPoolMXBean() != null) {
            dataSourceInfoVo.setIdleConnections(datasource.getHikariPoolMXBean().getIdleConnections());
            dataSourceInfoVo.setActiveConnections(datasource.getHikariPoolMXBean().getActiveConnections());
            dataSourceInfoVo.setThreadsAwaitingConnection(datasource.getHikariPoolMXBean().getThreadsAwaitingConnection());
            dataSourceInfoVo.setTotalConnections(datasource.getHikariPoolMXBean().getTotalConnections());
        }
        return dataSourceInfoVo;
    }


}
