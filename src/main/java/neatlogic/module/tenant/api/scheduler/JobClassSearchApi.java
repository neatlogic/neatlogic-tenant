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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.SCHEDULE_JOB_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.ModuleUtil;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.scheduler.core.SchedulerManager;
import neatlogic.framework.scheduler.dto.JobClassVo;
import neatlogic.framework.util.TableResultUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = SCHEDULE_JOB_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class JobClassSearchApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "job/class/search";
    }

    @Override
    public String getName() {
        return "nmtas.jobclasssearchapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword"),
            @Param(name = "moduleId", type = ApiParamType.STRING, desc = "term.cmdb.moduleid")})
    @Description(desc = "nmtas.jobclasssearchapi.getname")
    @Output({
            @Param(explode = JobClassVo.class, desc = "nmtas.jobclasssearchapi.output.param.desc")})
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JobClassVo jobClassVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<JobClassVo>() {
        });
        List<JobClassVo> jobClassList = searchJobClassList(jobClassVo);
        return TableResultUtil.getResult(jobClassList, jobClassVo);
    }

    private List<JobClassVo> searchJobClassList(JobClassVo jobClassVo) {
        List<JobClassVo> jobClassList = SchedulerManager.getAllPublicJobClassList();
        List<JobClassVo> jobClassFilterList = new ArrayList<>();
        List<String> moduleList = new ArrayList<>();
        if (StringUtils.isNotBlank(jobClassVo.getModuleId())) {
            moduleList = ModuleUtil.getModuleGroup(jobClassVo.getModuleId()).getModuleIdList();
        }
        for (JobClassVo jobClass : jobClassList) {
            if (!TenantContext.get().containsModule(jobClass.getModuleId())) {
                continue;
            }
            if (CollectionUtils.isNotEmpty(moduleList) && !moduleList.contains(jobClass.getModuleId())) {
                continue;
            }
            if (jobClassVo.getKeyword() != null && !jobClass.getName().toUpperCase().contains(jobClassVo.getKeyword().toUpperCase())) {
                continue;
            }
            jobClassFilterList.add(jobClass);
        }

        int pageSize = jobClassVo.getPageSize();
        int rowNum = jobClassFilterList.size();
        int pageCount = PageUtil.getPageCount(rowNum, pageSize);
        jobClassVo.setPageCount(pageCount);
        jobClassVo.setRowNum(rowNum);
        int startNum = jobClassVo.getStartNum();
        int endNum = startNum + pageSize;
        endNum = Math.min(endNum, rowNum);
        return jobClassFilterList.subList(startNum, endNum);
    }

}
