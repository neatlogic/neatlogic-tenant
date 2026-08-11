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
import neatlogic.framework.asynchronization.threadlocal.UserContext;
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
import neatlogic.framework.scheduler.dto.ScheduleJobSourceVo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 单行或批量修改定时作业来源的服务器组。
 */
@Service
@AuthAction(action = SCHEDULE_JOB_MODIFY.class)
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
public class JobSourceServerGroupUpdateApi extends PrivateApiComponentBase {

    @Resource
    private SchedulerMapper schedulerMapper;

    @Override
    public String getToken() {
        return "job/source/servergroup/update";
    }

    @Override
    public String getName() {
        return "修改定时作业来源服务器组";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "jobSourceList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "待修改的作业来源列表"),
            @Param(name = "serverGroup", type = ApiParamType.STRING, maxLength = 99, desc = "目标服务器组，空值表示不指定服务器组")
    })
    @Description(desc = "修改定时作业来源服务器组")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<ScheduleJobSourceVo> jobSourceList = jsonObj.getJSONArray("jobSourceList").toJavaList(ScheduleJobSourceVo.class);
        // 空列表不生成MyBatis foreach SQL，避免出现空的IN条件。
        if (CollectionUtils.isEmpty(jobSourceList)) {
            return null;
        }
        String serverGroup = jsonObj.getString("serverGroup");
        // 数据库字段长度为100，后端同步校验可防止绕过前端直接提交超长值。
//        if (StringUtils.length(serverGroup) > 100) {
//            throw new ApiRuntimeException("服务器组长度不能超过100个字符");
//        }
//        schedulerMapper.updateJobSourceServerGroup(jobSourceList, serverGroup);
        for (ScheduleJobSourceVo jobSourceVo : jobSourceList) {
            jobSourceVo.setServerId(-1);
            jobSourceVo.setFcu(UserContext.get().getUserUuid());
            jobSourceVo.setServerGroup(serverGroup);
        }
        schedulerMapper.insertJobSourceList(jobSourceList);
        return null;
    }
}
