/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.tenant.api.scheduler;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.SCHEDULE_JOB_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.scheduler.dao.mapper.SchedulerMapper;
import neatlogic.framework.scheduler.dto.JobAuditVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@AuthAction(action = SCHEDULE_JOB_MODIFY.class)
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
            @Param(name = "jobUuid", type = ApiParamType.STRING, isRequired = true, desc = "定时作业uuid，不提供则搜索所有作业的执行记录")})
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
