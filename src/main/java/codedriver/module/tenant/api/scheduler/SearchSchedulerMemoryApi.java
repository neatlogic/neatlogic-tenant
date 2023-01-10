/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */
package codedriver.module.tenant.api.scheduler;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.SCHEDULE_JOB_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.exception.type.ParamNotExistsException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.scheduler.dto.JobAuditVo;
import codedriver.framework.scheduler.dto.JobObject;
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
            @Param(name = "tbodyList", explode = JobAuditVo[].class, desc = "内存的定时作业列表")
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
        } else if (StringUtils.isEmpty(jobGroupName)) {
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

        for (int offset = 0; offset < returnList.size(); offset += pageSize) {
            if (offset == pageSize * (currentPage - 1)) {
                returnList = returnList.stream()
                        .skip(offset)
                        .limit(pageSize)
                        .collect(Collectors.toList());
            }
        }
        resultObj.put("tbodyList", returnList);
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
