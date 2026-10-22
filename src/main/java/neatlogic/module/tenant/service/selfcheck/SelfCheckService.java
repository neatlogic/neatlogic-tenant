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
package neatlogic.module.tenant.service.selfcheck;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.selfcheck.dto.SelfCheckScriptResultVo;
import neatlogic.module.tenant.dto.selfcheck.SelfCheckRunItemVo;
import neatlogic.module.tenant.dto.selfcheck.SelfCheckRunVo;
import neatlogic.module.tenant.dto.selfcheck.SelfCheckScenarioVo;

public interface SelfCheckService {
    /** 查询显式准入 API 与平台内置基础服务目标。 */
    JSONObject searchTarget(JSONObject paramObj);
    /** 保存场景并使用后端脚本引擎重新校验全部检查项。 */
    SelfCheckScenarioVo saveScenario(SelfCheckScenarioVo scenarioVo);
    /** 获取场景及其有序检查项。 */
    SelfCheckScenarioVo getScenario(Long id);
    /** 分页查询场景。 */
    JSONObject searchScenario(SelfCheckScenarioVo searchVo);
    /** 删除不在执行中的场景配置。 */
    void deleteScenario(Long id);
    /** 通过乐观锁更新场景状态。 */
    void updateScenarioStatus(Long id, String status, Integer version);
    /** 只编译脚本，不执行目标。 */
    SelfCheckScriptResultVo validateScript(String script);
    /** 按正式执行链路调试未保存的单个检查项。 */
    SelfCheckRunItemVo testItem(JSONObject paramObj);
    /** 创建异步场景执行并返回待执行记录。 */
    SelfCheckRunVo createRun(Long scenarioId);
    /** 分页查询执行历史。 */
    JSONObject searchRun(SelfCheckRunVo searchVo);
    /** 获取执行及各检查项的完整阶段证据。 */
    JSONObject getRun(Long runId);
}
