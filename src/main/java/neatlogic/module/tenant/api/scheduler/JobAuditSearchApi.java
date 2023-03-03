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
            @Param(name = "jobUuid", type = ApiParamType.STRING, desc = "定时作业uuid，不提供则搜索所有作业的执行记录")})
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
