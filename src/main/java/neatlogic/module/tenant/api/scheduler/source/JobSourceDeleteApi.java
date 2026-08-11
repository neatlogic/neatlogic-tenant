/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.tenant.api.scheduler.source;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.SCHEDULE_JOB_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.scheduler.dao.mapper.SchedulerMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 删除指定定时作业的来源记录，不删除作业配置及运行记录。
 */
@Service
@AuthAction(action = SCHEDULE_JOB_MODIFY.class)
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
public class JobSourceDeleteApi extends PrivateApiComponentBase {

    @Resource
    private SchedulerMapper schedulerMapper;

    @Override
    public String getToken() {
        return "job/source/delete";
    }

    @Override
    public String getName() {
        return "删除定时作业来源";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "jobName", type = ApiParamType.STRING, isRequired = true, desc = "作业唯一标识"),
            @Param(name = "jobGroup", type = ApiParamType.STRING, isRequired = true, desc = "作业组")
    })
    @Description(desc = "删除定时作业来源")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        // 按schedule_job_source复合主键精确删除，避免同名作业组之间相互影响。
        String jobName = jsonObj.getString("jobName");
        String jobGroup = jsonObj.getString("jobGroup");
        schedulerMapper.deleteJobSourceByJobNameAndJobGroup(jobName, jobGroup);
        return null;
    }
}
