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
package neatlogic.module.tenant.dao.mapper;

import neatlogic.module.tenant.dto.selfcheck.SelfCheckRunItemVo;
import neatlogic.module.tenant.dto.selfcheck.SelfCheckRunVo;
import neatlogic.module.tenant.dto.selfcheck.SelfCheckScenarioItemVo;
import neatlogic.module.tenant.dto.selfcheck.SelfCheckScenarioVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SelfCheckMapper {
    /** 按 ID 查询场景。 */
    SelfCheckScenarioVo getScenarioById(Long id);
    /** 按 ID 锁定并查询场景。 */
    SelfCheckScenarioVo getScenarioLockById(Long id);
    /** 分页查询场景。 */
    List<SelfCheckScenarioVo> searchScenario(SelfCheckScenarioVo searchVo);
    /** 统计场景数量。 */
    int searchScenarioCount(SelfCheckScenarioVo searchVo);
    /** 新增场景。 */
    int insertScenario(SelfCheckScenarioVo scenarioVo);
    /** 按版本更新场景。 */
    int updateScenario(SelfCheckScenarioVo scenarioVo);
    /** 按版本更新场景状态。 */
    int updateScenarioStatus(@Param("id") Long id, @Param("status") String status,
                             @Param("version") Integer version, @Param("lcu") String lcu);
    /** 删除场景。 */
    int deleteScenarioById(Long id);

    /** 查询场景检查项。 */
    List<SelfCheckScenarioItemVo> getScenarioItemListByScenarioId(Long scenarioId);
    /** 新增场景检查项。 */
    int insertScenarioItem(SelfCheckScenarioItemVo itemVo);
    /** 删除场景检查项。 */
    int deleteScenarioItemByScenarioId(Long scenarioId);

    /** 按 ID 查询执行记录。 */
    SelfCheckRunVo getRunById(Long id);
    /** 分页查询执行记录。 */
    List<SelfCheckRunVo> searchRun(SelfCheckRunVo searchVo);
    /** 统计执行记录数量。 */
    int searchRunCount(SelfCheckRunVo searchVo);
    /** 统计场景未结束的执行数量。 */
    int countRunningRunByScenarioId(Long scenarioId);
    /** 新增执行记录。 */
    int insertRun(SelfCheckRunVo runVo);
    /** 更新执行记录。 */
    int updateRun(SelfCheckRunVo runVo);
    /** 终止指定节点遗留的执行检查项。 */
    int abortUnfinishedRunItem(@Param("serverId") Integer serverId);
    /** 终止指定节点遗留的执行记录。 */
    int abortUnfinishedRun(@Param("serverId") Integer serverId);

    /** 查询执行检查项。 */
    List<SelfCheckRunItemVo> getRunItemListByRunId(Long runId);
    /** 新增执行检查项。 */
    int insertRunItem(SelfCheckRunItemVo itemVo);
    /** 更新执行检查项。 */
    int updateRunItem(SelfCheckRunItemVo itemVo);

    /** 通过当前租户数据源执行数据库探针。 */
    Integer checkMysqlConnection();
}
