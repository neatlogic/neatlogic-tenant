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
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.SCHEDULE_JOB_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.ModuleUtil;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dto.module.ModuleGroupVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.scheduler.core.SchedulerManager;
import neatlogic.framework.scheduler.dao.mapper.SchedulerMapper;
import neatlogic.framework.scheduler.dto.*;
import neatlogic.framework.scheduler.exception.ScheduleHandlerNotFoundException;
import neatlogic.framework.util.$;
import neatlogic.framework.util.TableResultUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AuthAction(action = SCHEDULE_JOB_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class JobSourceSearchApi extends PrivateApiComponentBase {

    @Resource
    private SchedulerMapper schedulerMapper;

    @Override
    public String getToken() {
        return "job/source/search";
    }

    @Override
    public String getName() {
        return "nmtass.jobsourcesearchapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword"),
            @Param(name = "handler", type = ApiParamType.STRING, desc = "common.handler"),
            @Param(name = "moduleId", type = ApiParamType.STRING, desc = "common.modulegroupa"),
    })
    @Output({
            @Param(name = "tbodyList", explode = ScheduleJobSourceVo[].class, desc = "common.tbodylist"),
    })
    @Description(desc = "nmtass.jobsourcesearchapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        BasePageVo searchVo = JSONObject.toJavaObject(jsonObj, BasePageVo.class);
        Set<String> handlerSet = new HashSet<>();
        String handler = jsonObj.getString("handler");
        if (StringUtils.isNotBlank(handler)) {
            JobClassVo jobClass = SchedulerManager.getJobClassByClassName(handler);
            if (jobClass == null) {
                throw new ScheduleHandlerNotFoundException(handler);
            }
            handlerSet.add(handler);
        }
        String moduleId = jsonObj.getString("moduleId");
        if (StringUtils.isNotBlank(moduleId)) {
            List<String> moduleIdList = new ArrayList<>();
            ModuleGroupVo moduleGroupVo = ModuleUtil.getModuleGroup(moduleId);
            if (moduleGroupVo != null) {
                moduleIdList = moduleGroupVo.getModuleIdList();
            }
            List<String> finalModuleIdList = moduleIdList;
            List<String> list = SchedulerManager.getAllJobClassList().stream()
                    .filter(jobClassVo -> TenantContext.get().containsModule(jobClassVo.getModuleId()))
                    .filter(jobClassVo -> CollectionUtils.isNotEmpty(finalModuleIdList) && finalModuleIdList.contains(jobClassVo.getModuleId()))
                    .map(JobClassVo::getClassName)
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(list)) {
                return TableResultUtil.getResult(new ArrayList(), searchVo);
            } else {
                if (StringUtils.isNotBlank(handler)) {
                    if (!list.contains(handler)) {
                        return TableResultUtil.getResult(new ArrayList(), searchVo);
                    }
                } else {
                    handlerSet.addAll(list);
                }
            }
        }
        List<ScheduleJobSourceVo> jobSourceList = searchJobSource(searchVo, handlerSet);
        JSONObject resultObj = TableResultUtil.getResult(jobSourceList, searchVo);
        return resultObj;
    }

    private List<ScheduleJobSourceVo> searchJobSource(BasePageVo searchVo, Set<String> handlerSet) {
        List<ScheduleJobSourceVo> resultList = new ArrayList<>();
        Map<String, ScheduleJobSourceVo> scheduleJobSourceMap = new HashMap<>();
        List<ScheduleJobSourceVo> allJobSourceList = schedulerMapper.getAllJobSourceList();
        for (ScheduleJobSourceVo jobSourceVo : allJobSourceList) {
            String key = jobSourceVo.getJobName() + "#" + jobSourceVo.getJobGroup();
            scheduleJobSourceMap.put(key, jobSourceVo);
        }
        List<JobLockVo> jobLockList = schedulerMapper.getAllJobLockList();
        for (JobLockVo jobLockVo : jobLockList) {
            String key = jobLockVo.getJobName() + "#" + jobLockVo.getJobGroup();
            ScheduleJobSourceVo jobSourceVo = scheduleJobSourceMap.remove(key);
            if (jobSourceVo != null) {
                jobSourceVo.setHandler(jobLockVo.getJobHandler());
            } else {
                jobSourceVo = new ScheduleJobSourceVo();
                jobSourceVo.setJobName(jobLockVo.getJobName());
                jobSourceVo.setJobGroup(jobLockVo.getJobGroup());
                jobSourceVo.setHandler(jobLockVo.getJobHandler());
            }
            if (CollectionUtils.isNotEmpty(handlerSet) && !handlerSet.contains(jobSourceVo.getHandler())) {
                continue;
            }
            JobClassVo jobClassVo = SchedulerManager.getJobClassByClassName(jobSourceVo.getHandler());
            if (jobClassVo != null && StringUtils.isNotBlank(jobClassVo.getName())) {
                jobSourceVo.setHandlerName($.t(jobClassVo.getName()));
            } else {
                jobSourceVo.setHandlerName(jobSourceVo.getHandler().substring(jobSourceVo.getHandler().lastIndexOf(".") + 1));
            }
            if (StringUtils.isNotBlank(searchVo.getKeyword())) {
                String keyword = searchVo.getKeyword().trim().toLowerCase();
                if (!jobSourceVo.getJobName().toLowerCase().contains(keyword)
                        && !jobSourceVo.getJobGroup().toLowerCase().contains(keyword)
                        && !jobSourceVo.getHandlerName().toLowerCase().contains(keyword)) {
                    continue;
                }
            }
            resultList.add(jobSourceVo);
        }
        if (CollectionUtils.isEmpty(handlerSet)) {
            for (Map.Entry<String, ScheduleJobSourceVo> entry : scheduleJobSourceMap.entrySet()) {
                ScheduleJobSourceVo jobSourceVo = entry.getValue();
                if (StringUtils.isNotBlank(searchVo.getKeyword())) {
                    String keyword = searchVo.getKeyword().trim().toLowerCase();
                    if (!jobSourceVo.getJobName().toLowerCase().contains(keyword)
                            && !jobSourceVo.getJobGroup().toLowerCase().contains(keyword)) {
                        continue;
                    }
                }
                resultList.add(jobSourceVo);
            }
        }
        resultList.sort((o1, o2) -> {
            int i = o1.getJobGroup().compareTo(o2.getJobGroup());
            if (i == 0) {
                return o1.getJobName().compareTo(o2.getJobName());
            }
            return i;
        });
        searchVo.setRowNum(resultList.size());
        return PageUtil.subList(resultList, searchVo);
    }
}
