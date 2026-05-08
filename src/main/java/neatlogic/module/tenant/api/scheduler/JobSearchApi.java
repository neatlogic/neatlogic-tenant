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
import neatlogic.framework.scheduler.dto.JobVo;
import neatlogic.framework.scheduler.exception.ScheduleHandlerNotFoundException;
import neatlogic.framework.util.TableResultUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AuthAction(action = SCHEDULE_JOB_MODIFY.class)

@OperationType(type = OperationTypeEnum.SEARCH)
public class JobSearchApi extends PrivateApiComponentBase {

    @Resource
    private SchedulerMapper schedulerMapper;

    @Override
    public String getToken() {
        return "job/search";
    }

    @Override
    public String getName() {
        return "nmtas.jobsearchapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword"),
            @Param(name = "handler", type = ApiParamType.STRING, desc = "nmtas.jobsearchapi.input.param.desc.handler"),
            @Param(name = "moduleId", type = ApiParamType.STRING, desc = "term.cmdb.moduleid"),
            @Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", desc = "状态(0:禁用，1:启用)"),
            @Param(name = "needAudit", type = ApiParamType.ENUM, rule = "0,1", desc = "是否保存执行记录(0:不保存，1:保存)")
    })
    @Description(desc = "nmtas.jobsearchapi.getname")
    @Output({
            @Param(explode = JobVo.class, desc = "nmtas.jobsearchapi.output.param.desc")
    })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        // 判断定时作业组件是否存在
        if (jsonObj.containsKey("handler")) {
            String handler = jsonObj.getString("handler");
            JobClassVo jobClass = SchedulerManager.getJobClassByClassName(handler);
            if (jobClass == null) {
                throw new ScheduleHandlerNotFoundException(handler);
            }
        }

        JobVo jobVo = JSONObject.toJavaObject(jsonObj, JobVo.class);
        String moduleId = jsonObj.getString("moduleId");
        if (StringUtils.isNotBlank(moduleId)) {
            List<String> moduleIdList = new ArrayList<>();
            ModuleGroupVo moduleGroupVo = ModuleUtil.getModuleGroup(moduleId);
            if (moduleGroupVo != null) {
                moduleIdList = moduleGroupVo.getModuleIdList();
            }
            List<String> finalModuleIdList = moduleIdList;
            List<String> handlerList = SchedulerManager.getAllJobClassList().stream()
                    .filter(jobClassVo -> TenantContext.get().containsModule(jobClassVo.getModuleId()))
                    .filter(jobClassVo -> CollectionUtils.isNotEmpty(finalModuleIdList) && finalModuleIdList.contains(jobClassVo.getModuleId()))
                    .map(JobClassVo::getClassName)
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(handlerList)) {
                jobVo.setRowNum(0);
                jobVo.setPageCount(0);
                return TableResultUtil.getResult(new ArrayList<>(), jobVo);
            }
            jobVo.setHandlerList(handlerList);
        }
        int rowNum = schedulerMapper.searchJobCount(jobVo);
        int pageCount = PageUtil.getPageCount(rowNum, jobVo.getPageSize());
        jobVo.setPageCount(pageCount);
        jobVo.setRowNum(rowNum);
        List<JobVo> jobList = new ArrayList<>();
        if (rowNum > 0) {
            jobList = schedulerMapper.searchJob(jobVo);
        }
        return TableResultUtil.getResult(jobList, jobVo);
    }

}
