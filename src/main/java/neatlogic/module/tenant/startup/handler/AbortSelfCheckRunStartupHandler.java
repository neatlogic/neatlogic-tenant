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
package neatlogic.module.tenant.startup.handler;

import neatlogic.framework.common.config.Config;
import neatlogic.framework.startup.StartupBase;
import neatlogic.module.tenant.dao.mapper.SelfCheckMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 服务启动时终止当前节点遗留的自检执行记录。
 */
@Service
public class AbortSelfCheckRunStartupHandler extends StartupBase {

    @Resource
    private SelfCheckMapper selfCheckMapper;

    /**
     * 返回启动处理器名称。
     *
     * @return 处理器名称
     */
    @Override
    public String getName() {
        return "终止当前节点遗留的功能自检任务";
    }

    /**
     * 在常规业务恢复处理器之前清理遗留状态。
     *
     * @return 执行顺序
     */
    @Override
    public int sort() {
        return 3;
    }

    /**
     * 仅终止当前租户、当前服务节点的遗留任务，避免影响集群其他节点。
     *
     * @return 被终止的记录数
     */
    @Override
    @Transactional
    public int executeForCurrentTenant() {
        int count = selfCheckMapper.abortUnfinishedRunItem(Config.SCHEDULE_SERVER_ID);
        count += selfCheckMapper.abortUnfinishedRun(Config.SCHEDULE_SERVER_ID);
        return count;
    }
}
