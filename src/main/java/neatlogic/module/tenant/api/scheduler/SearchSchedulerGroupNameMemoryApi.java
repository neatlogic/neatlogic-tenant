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

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.SCHEDULE_JOB_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author longrf
 * @date 2023/1/9 14:51
 */
@Service
@AuthAction(action = SCHEDULE_JOB_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchSchedulerGroupNameMemoryApi extends PrivateApiComponentBase {

    @Resource
    private SchedulerFactoryBean schedulerFactoryBean;

    @Override
    public String getName() {
        return "获取所有定时作业组的组名列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字")
    })
    @Output({
            @Param(explode = ValueTextVo[].class, desc = "定时作业组的组名列表")
    })
    @Description(desc = "获取所有定时作业组的组名列表")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String keyword = paramObj.getString("keyword");
        List<ValueTextVo> returnList = new ArrayList<>();
        int length = TenantContext.get().getTenantUuid().length();
        for (String groupName : schedulerFactoryBean.getScheduler().getJobGroupNames()) {
            String text = groupName.substring(length + 1);
            if (text.contains(keyword)) {
                returnList.add(new ValueTextVo(groupName, text));
            }
        }
        return returnList;
    }

    @Override
    public String getToken() {
        return "scheduler/groupname/search";
    }
}


