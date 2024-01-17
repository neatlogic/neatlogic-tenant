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
