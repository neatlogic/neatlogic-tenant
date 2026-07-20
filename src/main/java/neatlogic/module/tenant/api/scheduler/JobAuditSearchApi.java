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
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.ModuleUtil;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dto.module.ModuleGroupVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.scheduler.core.SchedulerManager;
import neatlogic.framework.scheduler.dao.mapper.SchedulerMapper;
import neatlogic.framework.scheduler.dto.JobAuditVo;
import neatlogic.framework.scheduler.dto.JobClassVo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class JobAuditSearchApi extends PrivateApiComponentBase {

    @Autowired
    private SchedulerMapper schedulerMapper;

    @Override
    public String getToken() {
        return "job/audit/search";
    }

    @Override
    public String getName() {
        return "查询定时作业执行记录列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页码"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "页大小"),
            @Param(name = "jobUuid", type = ApiParamType.STRING, desc = "定时作业uuid，不提供则搜索所有作业的执行记录"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "作业名"),
            @Param(name = "moduleId", type = ApiParamType.STRING, desc = "所属模块id"),
            @Param(name = "jobHandler", type = ApiParamType.STRING, desc = "作业组件类路径"),
            @Param(name = "jobGroupName", type = ApiParamType.STRING, desc = "作业组名"),
            @Param(name = "status", type = ApiParamType.STRING, desc = "执行状态"),
            @Param(name = "startTimeRange", type = ApiParamType.JSONOBJECT, desc = "开始时间范围")})
    @Description(desc = "查询定时作业执行记录列表")
    @Output({
            @Param(name = "currentPage", type = ApiParamType.INTEGER, isRequired = true, desc = "当前页码"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, isRequired = true, desc = "页大小"),
            @Param(name = "pageCount", type = ApiParamType.INTEGER, isRequired = true, desc = "总页数"),
            @Param(name = "rowNum", type = ApiParamType.INTEGER, isRequired = true, desc = "总行数"),
            @Param(name = "tbodyList", explode = JobAuditVo[].class, desc = "执行记录列表")
    })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JobAuditVo jobAuditVo = JSONObject.toJavaObject(jsonObj, JobAuditVo.class);
        // 所属模块是运行时信息，需要先转换为对应的作业组件类路径，再交给数据库统一过滤。
        String moduleId = jsonObj.getString("moduleId");
        if (StringUtils.isNotBlank(moduleId)) {
            List<String> moduleIdList = new ArrayList<>();
            ModuleGroupVo moduleGroupVo = ModuleUtil.getModuleGroup(moduleId);
            if (moduleGroupVo != null) {
                moduleIdList = moduleGroupVo.getModuleIdList();
            }
            List<String> finalModuleIdList = moduleIdList;
            List<String> jobHandlerList = SchedulerManager.getAllJobClassList().stream()
                    .filter(jobClassVo -> TenantContext.get().containsModule(jobClassVo.getModuleId()))
                    .filter(jobClassVo -> CollectionUtils.isNotEmpty(finalModuleIdList) && finalModuleIdList.contains(jobClassVo.getModuleId()))
                    .map(JobClassVo::getClassName)
                    .collect(Collectors.toList());
            jobAuditVo.setJobHandlerList(jobHandlerList);
        }
        //通用接口无需校验
        /*if (StringUtils.isNotBlank(jobAuditVo.getJobUuid())) {
            JobVo job = schedulerMapper.getJobByUuid(jobAuditVo.getJobUuid());
            if (job == null) {
                throw new ScheduleJobNotFoundException(jobAuditVo.getJobUuid());
            }
        }*/
        int rowNum = schedulerMapper.searchJobAuditCount(jobAuditVo);
        int pageCount = PageUtil.getPageCount(rowNum, jobAuditVo.getPageSize());
        jobAuditVo.setPageCount(pageCount);
        jobAuditVo.setRowNum(rowNum);
        List<JobAuditVo> jobAuditList = schedulerMapper.searchJobAudit(jobAuditVo);

        JSONObject resultObj = new JSONObject();
        resultObj.put("tbodyList", jobAuditList);
        resultObj.put("currentPage", jobAuditVo.getCurrentPage());
        resultObj.put("pageSize", jobAuditVo.getPageSize());
        resultObj.put("pageCount", jobAuditVo.getPageCount());
        resultObj.put("rowNum", jobAuditVo.getRowNum());
        return resultObj;
    }

}
