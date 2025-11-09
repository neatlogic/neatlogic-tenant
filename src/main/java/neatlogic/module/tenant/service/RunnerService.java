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
package neatlogic.module.tenant.service;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.dto.runner.RunnerVo;

public interface RunnerService {
    /**
     * 保存runner
     * @param runner 入惨runner对象
     * @param paramRunnerId 入参runner id
     */
    void SaveRunner(RunnerVo runner,Long paramRunnerId);

    /**
     * 主动检查runner状态
     * @param runnerId 指定检查runnerId
     */
    JSONObject checkRunnerHealth(Long runnerId);
}
