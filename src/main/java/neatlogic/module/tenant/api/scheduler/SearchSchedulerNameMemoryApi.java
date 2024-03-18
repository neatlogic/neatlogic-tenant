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

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.SCHEDULE_JOB_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author longrf
 * @date 2023/1/10 11:40
 */

@Service
@AuthAction(action = SCHEDULE_JOB_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchSchedulerNameMemoryApi extends PrivateApiComponentBase {

    @Resource
    private SchedulerFactoryBean schedulerFactoryBean;

    @Override
    public String getName() {
        return "获取定时作业名称列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "jobGroupName", type = ApiParamType.STRING, isRequired = true, desc = "作业组名"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字")
    })
    @Output({
            @Param(explode = ValueTextVo[].class, desc = "定时作业名称列表")
    })
    @Description(desc = "获取定时作业名称列表")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String jobGroupName = paramObj.getString("jobGroupName");
        String keyword = paramObj.getString("keyword");
        List<ValueTextVo> returnList = new ArrayList<>();
        String tenantString = TenantContext.get().getTenantUuid();
        int length = tenantString.length();
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(jobGroupName))) {
            if (StringUtils.isNotEmpty(jobKey.getName())) {
                if (StringUtils.isNotEmpty(keyword)) {
                    if (jobKey.getName().contains(keyword)) {
                        returnList.add(new ValueTextVo(jobKey.getName(), jobKey.getName().startsWith(tenantString) ? jobKey.getName().substring(length + 1) : jobKey.getName()));
                    }
                } else {
                    returnList.add(new ValueTextVo(jobKey.getName(), jobKey.getName().startsWith(tenantString) ? jobKey.getName().substring(length + 1) : jobKey.getName()));
                }
            }
        }
        return returnList;
    }

    @Override
    public String getToken() {
        return "scheduler/name/search";
    }
}
