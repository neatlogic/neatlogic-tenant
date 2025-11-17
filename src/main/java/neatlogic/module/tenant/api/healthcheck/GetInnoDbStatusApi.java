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
import neatlogic.framework.dao.mapper.healthcheck.SqlStatusMapper;
import neatlogic.framework.dto.healthcheck.DataSourceInfoVo;
import neatlogic.framework.dto.healthcheck.InnoDbStatusVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.store.mysql.DatabaseVendor;
import neatlogic.framework.store.mysql.DatasourceManager;
import neatlogic.framework.util.JdbcUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetInnoDbStatusApi extends PrivateApiComponentBase {
    @Resource
    private SqlStatusMapper sqlStatusMapper;

    @Override
    public String getToken() {
        return "/healthcheck/innodb/status";
    }

    @Override
    public String getName() {
        return "获取Innodb引擎状态";
    }

    @Override
    public String getConfig() {
        return null;
    }


    @Output({
            @Param(explode = DataSourceInfoVo.class),
    })
    @Description(desc = "获取Innodb引擎状态接口，用于查看死锁信息")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        StringBuilder stringBuilder = new StringBuilder();
        String databaseId = DatasourceManager.getDatabaseId();
        if (Objects.equals(databaseId, DatabaseVendor.MYSQL.getDatabaseId())) {
            InnoDbStatusVo innoDbStatusVo = sqlStatusMapper.getInnodbStatus();
            if (innoDbStatusVo != null) {
                stringBuilder.append(innoDbStatusVo.getStatus());
            }
            // 当前所有连接及其状态
            String sql = "SHOW PROCESSLIST";
            List<Map<String, Object>> linkedHashMapList = sqlStatusMapper.selectListBySql(sql);
            stringBuilder.append(JdbcUtil.dataVisualization(sql, linkedHashMapList));
            // Threads_connected(当前已连接的线程数) THREADS_RUNNING(当前正在执行的线程数，即正在处理查询或操作的线程数) Max_used_connections(同时存在的最大连接数)
            sql = "SELECT * FROM `performance_schema`.`global_status` WHERE `VARIABLE_NAME` IN ('Threads_connected', 'THREADS_RUNNING', 'Max_used_connections')";
            linkedHashMapList = sqlStatusMapper.selectListBySql(sql);
            stringBuilder.append(JdbcUtil.dataVisualization(sql, linkedHashMapList));
            // max_connections(MySQL 允许的最大连接数) max_user_connections(限制每个用户的最大并发连接数) max_allowed_packet(客户端服务器之间通信的最大包大小)
            sql = "SELECT * FROM `performance_schema`.`global_variables` WHERE `VARIABLE_NAME` IN ('max_connections', 'max_user_connections', 'max_allowed_packet')";
            linkedHashMapList = sqlStatusMapper.selectListBySql(sql);
            stringBuilder.append(JdbcUtil.dataVisualization(sql, linkedHashMapList));
            // 正在执行的 InnoDB 事务的详细信息
            sql = "SELECT * FROM `information_schema`.`INNODB_TRX`";
            linkedHashMapList = sqlStatusMapper.selectListBySql(sql);
            stringBuilder.append(JdbcUtil.dataVisualization(sql, linkedHashMapList));
        } else if (Objects.equals(databaseId, DatabaseVendor.OCEAN_BASE.getDatabaseId())) {
            String sql = "SHOW ENGINE OCEANBASE STATUS";
            List<Map<String, Object>> linkedHashMapList = sqlStatusMapper.selectListBySql(sql);
            if (CollectionUtils.isNotEmpty(linkedHashMapList)) {
                Object status = linkedHashMapList.get(0).get("Status");
                if (status != null) {
                    stringBuilder.append(status);
                }
            }
            // 当前所有连接及其状态
            sql = "SHOW PROCESSLIST";
            linkedHashMapList = sqlStatusMapper.selectListBySql(sql);
            stringBuilder.append(JdbcUtil.dataVisualization(sql, linkedHashMapList));
            // 分布式事务详情（需系统权限）
            sql = "SELECT * FROM `oceanbase`.__all_virtual_trans_stat";
            linkedHashMapList = sqlStatusMapper.selectListBySql(sql);
            stringBuilder.append(JdbcUtil.dataVisualization(sql, linkedHashMapList));
            // 内存统计
            sql = "SELECT * FROM `oceanbase`.__all_virtual_sysstat WHERE NAME LIKE '%memory%'";
            linkedHashMapList = sqlStatusMapper.selectListBySql(sql);
            stringBuilder.append(JdbcUtil.dataVisualization(sql, linkedHashMapList));
            // 死锁历史
            sql = "SELECT * FROM `oceanbase`.__all_virtual_deadlock_event_history";
            linkedHashMapList = sqlStatusMapper.selectListBySql(sql);
            stringBuilder.append(JdbcUtil.dataVisualization(sql, linkedHashMapList));
        } else if (Objects.equals(databaseId, DatabaseVendor.TIDB.getDatabaseId())) {
            String sql = "SHOW PROCESSLIST";
            List<Map<String, Object>> linkedHashMapList = sqlStatusMapper.selectListBySql(sql);
            stringBuilder.append(JdbcUtil.dataVisualization(sql, linkedHashMapList));
            // TiDB 节点的一些内部状态信息，包括一些类似于存储引擎的状态变量
            sql = "SHOW STORE STATUS";
            linkedHashMapList = sqlStatusMapper.selectListBySql(sql);
            stringBuilder.append(JdbcUtil.dataVisualization(sql, linkedHashMapList));
        }
        resultObj.put("status", stringBuilder.toString());
        return resultObj;
    }


}
