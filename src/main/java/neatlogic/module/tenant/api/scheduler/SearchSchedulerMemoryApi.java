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
package neatlogic.module.tenant.api.scheduler;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.SCHEDULE_JOB_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.ModuleUtil;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dto.module.ModuleGroupVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.scheduler.core.SchedulerManager;
import neatlogic.framework.scheduler.dao.mapper.SchedulerMapper;
import neatlogic.framework.scheduler.dto.JobClassVo;
import neatlogic.framework.scheduler.dto.JobInfoVo;
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.framework.scheduler.dto.JobStatusVo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    @Resource
    private SchedulerMapper schedulerMapper;

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
            @Param(name = "jobGroupName", type = ApiParamType.STRING, desc = "作业组名"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字"),
            @Param(name = "handler", type = ApiParamType.STRING, desc = "作业组件"),
            @Param(name = "moduleId", type = ApiParamType.STRING, desc = "term.cmdb.moduleid"),
            @Param(name = "state", type = ApiParamType.STRING, desc = "作业状态"),
            @Param(name = "needAudit", type = ApiParamType.ENUM, rule = "0,1", desc = "是否保存执行记录(0:不保存，1:保存)")
    })
    @Output({
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页码"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "页大小"),
            @Param(name = "pageCount", type = ApiParamType.INTEGER, desc = "总页数"),
            @Param(name = "rowNum", type = ApiParamType.INTEGER, desc = "总行数"),
            @Param(name = "tbodyList", explode = JobInfoVo[].class, desc = "内存的定时作业列表")
    })
    @Description(desc = "查询内存的定时作业列表")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String jobGroupName = paramObj.getString("jobGroupName");
        String jobName = paramObj.getString("jobName");
        String keyword = paramObj.getString("keyword");
        String handler = paramObj.getString("handler");
        String moduleId = paramObj.getString("moduleId");
        String state = paramObj.getString("state");
        Integer needAudit = paramObj.getInteger("needAudit");
        List<String> moduleIdList = new ArrayList<>();
        if (StringUtils.isNotBlank(moduleId)) {
            ModuleGroupVo moduleGroupVo = ModuleUtil.getModuleGroup(moduleId);
            if (moduleGroupVo != null) {
                moduleIdList = moduleGroupVo.getModuleIdList();
            }
        }
        List<JobInfoVo> returnList = getAllJob();
        List<String> finalModuleIdList = moduleIdList;
        returnList = returnList.stream().filter(j -> (StringUtils.isBlank(jobGroupName) || jobGroupName.equals(j.getJobGroup()))
                && (StringUtils.isBlank(jobName) || jobName.equals(j.getJobName()))
                && (StringUtils.isBlank(handler) || handler.equals(j.getJobHandler()))
                && (StringUtils.isBlank(moduleId) || (CollectionUtils.isNotEmpty(finalModuleIdList) && finalModuleIdList.contains(j.getModuleId())))
                && (StringUtils.isBlank(state) || state.equals(j.getState()))
                && (needAudit == null || needAudit.equals(j.getNeedAudit()))
                && (StringUtils.isBlank(keyword) || StringUtils.contains(j.getJobName(), keyword) || StringUtils.contains(j.getJobGroup(), keyword) || StringUtils.contains(j.getJobHandler(), keyword))
        ).filter(e -> Objects.equals(e.getTenantUuid(), TenantContext.get().getTenantUuid())).collect(Collectors.toList());

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
     * @return 作业信息列表
     * @throws SchedulerException e
     */
    private List<JobInfoVo> getAllJob() throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        List<JobInfoVo> returnList = new ArrayList<>();
        for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.anyGroup())) {
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            JobObject jobObject = (JobObject) jobDetail.getJobDataMap().get("jobObject");
            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
            Trigger trigger = null;
            if (CollectionUtils.isNotEmpty(triggers)) {
                trigger = triggers.get(0);
            }
            if (jobObject != null) {
                JobInfoVo jobInfoVo = new JobInfoVo(jobObject);
                JobStatusVo jobStatusVo = schedulerMapper.getJobStatusByJobNameGroup(jobObject.getJobName(), jobObject.getJobGroup(), System.currentTimeMillis());
                if (jobStatusVo != null) {
                    jobInfoVo.setExecCount(jobStatusVo.getExecCount());
                    jobInfoVo.setLastFireTime(jobStatusVo.getLastFireTime());
                    jobInfoVo.setLastFinishTime(jobStatusVo.getLastFinishTime());
                }
                if (trigger != null) {
                    Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                    jobInfoVo.setState(triggerState.name());
                    jobInfoVo.setNextFireTime(trigger.getNextFireTime());
                    if (jobInfoVo.getLastFireTime() == null) {
                        jobInfoVo.setLastFireTime(trigger.getPreviousFireTime());
                    }
                }
                JobClassVo jobClassVo = SchedulerManager.getJobClassByClassName(jobObject.getJobHandler());
                if (jobClassVo != null && StringUtils.isNotBlank(jobClassVo.getName())) {
                    jobInfoVo.setJobHandlerName(jobClassVo.getName());
                }
                returnList.add(jobInfoVo);
            }
        }
        return returnList;
    }
}
