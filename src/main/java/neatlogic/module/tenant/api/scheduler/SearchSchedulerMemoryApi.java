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
package neatlogic.module.tenant.api.scheduler;

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.SCHEDULE_JOB_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.exception.type.ParamNotExistsException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.scheduler.dto.JobObject;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author longrf
 * @date 2023/1/5 17:01
 */

@Service
@AuthAction(action = SCHEDULE_JOB_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchSchedulerMemoryApi extends PrivateApiComponentBase {

    @Resource
    private SchedulerFactoryBean schedulerFactoryBean;

    @Override
    public String getName() {
        return "查询内存的定时作业列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    public String getToken() {
        return "scheduler/memory/search";
    }

    @Input({@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页码"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "页大小"),
            @Param(name = "jobName", type = ApiParamType.STRING, desc = "作业名(精确查询),jobName不为空时，jobGroupName也不可以为空"),
            @Param(name = "jobGroupName", type = ApiParamType.STRING, desc = "作业组名")
    })
    @Output({
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页码"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "页大小"),
            @Param(name = "pageCount", type = ApiParamType.INTEGER, desc = "总页数"),
            @Param(name = "rowNum", type = ApiParamType.INTEGER, desc = "总行数"),
            @Param(name = "tbodyList", explode = JobObject[].class, desc = "内存的定时作业列表")
    })
    @Description(desc = "查询内存的定时作业列表")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String jobGroupName = paramObj.getString("jobGroupName");
        String jobName = paramObj.getString("jobName");
        List<JobObject> returnList = new ArrayList<>();
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        if (StringUtils.isNotEmpty(jobName)) {
            if (StringUtils.isEmpty(jobGroupName)) {
                throw new ParamNotExistsException("jobGroupName");
            }
            TriggerKey triggerKey = new TriggerKey(jobName, jobGroupName);
            Trigger trigger = scheduler.getTrigger(triggerKey);
            if (trigger != null) {
                JobDetail jobDetail = scheduler.getJobDetail(trigger.getJobKey());
                JobObject jobObject = (JobObject) jobDetail.getJobDataMap().get("jobObject");
                if (jobObject != null) {
                    returnList.add(jobObject);
                }
            }
        } else if (StringUtils.isNotEmpty(jobGroupName)) {
            returnList = matchJobObject(scheduler, jobGroupName);
        } else {
            for (String groupName : scheduler.getJobGroupNames()) {
                returnList.addAll(matchJobObject(scheduler, groupName));
            }
        }

        JSONObject resultObj = new JSONObject();
        int currentPage = paramObj.getInteger("currentPage") != null ? paramObj.getInteger("currentPage") : 1;
        int pageSize = paramObj.getInteger("pageSize") != null ? paramObj.getInteger("pageSize") : 20;
        resultObj.put("rowNum", returnList.size());
        resultObj.put("pageCount", PageUtil.getPageCount(returnList.size(), pageSize));
        resultObj.put("currentPage", currentPage);
        resultObj.put("pageSize", pageSize);
        resultObj.put("tenant", TenantContext.get().getTenantUuid());
        resultObj.put("tbodyList", returnList.stream().skip((long) pageSize * (currentPage - 1)).limit(pageSize).collect(Collectors.toList()));
        return resultObj;
    }

    /**
     * 获取对应的定时作业信息列表
     *
     * @param scheduler scheduler
     * @param groupName 作业组名
     * @return 作业信息列表
     * @throws SchedulerException e
     */
    private List<JobObject> matchJobObject(Scheduler scheduler, String groupName) throws SchedulerException {
        List<JobObject> returnList = new ArrayList<>();
        for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            JobObject jobObject = (JobObject) jobDetail.getJobDataMap().get("jobObject");
            if (jobObject != null) {
                returnList.add(jobObject);
            }
        }
        return returnList;
    }
}
