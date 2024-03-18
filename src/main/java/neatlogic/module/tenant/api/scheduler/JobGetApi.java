/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tenant.api.scheduler;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.SCHEDULE_JOB_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.scheduler.core.IJob;
import neatlogic.framework.scheduler.core.SchedulerManager;
import neatlogic.framework.scheduler.dao.mapper.SchedulerMapper;
import neatlogic.framework.scheduler.dto.JobPropVo;
import neatlogic.framework.scheduler.dto.JobVo;
import neatlogic.framework.scheduler.exception.ScheduleHandlerNotFoundException;
import neatlogic.framework.scheduler.exception.ScheduleJobNotFoundException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AuthAction(action = SCHEDULE_JOB_MODIFY.class)

@OperationType(type = OperationTypeEnum.SEARCH)
public class JobGetApi extends PrivateApiComponentBase {

    @Autowired
    private SchedulerMapper schedulerMapper;

    @Override
    public String getToken() {
        return "job/get";
    }

    @Override
    public String getName() {
        return "nmtas.jobgetapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "common.jobuuid")
    })
    @Description(desc = "nmtas.jobgetapi.getname")
    @Output({
            @Param(name = "Return", explode = JobVo.class, desc = "common.schedulejobinfo"),
            @Param(name = "propList", explode = JobPropVo[].class, desc = "common.attributelist")
    })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String uuid = jsonObj.getString("uuid");
        JobVo jobVo = schedulerMapper.getJobByUuid(uuid);
        if (jobVo == null) {
            throw new ScheduleJobNotFoundException(uuid);
        }
        List<JobPropVo> propList = new ArrayList<>();
        IJob job = SchedulerManager.getHandler(jobVo.getHandler());
        if (job == null) {
            throw new ScheduleHandlerNotFoundException(jobVo.getHandler());
        }
        Map<String, neatlogic.framework.scheduler.annotation.Param> paramMap = job.initProp();
        for (Map.Entry<String, neatlogic.framework.scheduler.annotation.Param> entry : paramMap.entrySet()) {
            neatlogic.framework.scheduler.annotation.Param param = entry.getValue();
            JobPropVo jobPropVo = new JobPropVo();
            jobPropVo.setName(param.name());
            jobPropVo.setDataType(param.dataType());
            jobPropVo.setDescription(param.description());
            jobPropVo.setRequired(param.required());
            jobPropVo.setSort(param.sort());
            jobPropVo.setHelp(param.help());
            propList.add(jobPropVo);
        }
        //排序
        propList.sort(Comparator.comparing(JobPropVo::getSort, Comparator.nullsFirst(Comparator.naturalOrder())));
        if (CollectionUtils.isNotEmpty(jobVo.getPropList())) {
            Map<String, JobPropVo> jobPropMap = jobVo.getPropList().stream().collect(Collectors.toMap(e -> e.getName(), e -> e));
            for (JobPropVo jobPropVo : propList) {
                JobPropVo jobPropValueVo = jobPropMap.get(jobPropVo.getName());
                if (jobPropValueVo != null) {
                    jobPropVo.setId(jobPropValueVo.getId());
                    jobPropVo.setValue(jobPropValueVo.getValue());
                }
            }
        }
        jobVo.setPropList(propList);
        return jobVo;
    }

}
