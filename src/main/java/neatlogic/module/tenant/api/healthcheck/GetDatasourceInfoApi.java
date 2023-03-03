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

package neatlogic.module.tenant.api.healthcheck;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.dto.healthcheck.DataSourceInfoVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.store.mysql.NeatLogicBasicDataSource;
import neatlogic.framework.store.mysql.DatasourceManager;
import com.alibaba.fastjson.JSONObject;
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
