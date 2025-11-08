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
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.SCHEDULE_JOB_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.crossover.IScheduleCrossoverService;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

@AuthAction(action = SCHEDULE_JOB_MODIFY.class)
@Service
public class TestScheduleApi extends PrivateApiComponentBase {
    @Override
    public String getName() {
        return null;
    }

    @Input({
            @Param(name = "jobUuid", type = ApiParamType.STRING, desc = "nmtas.jobtestapi.input.param.desc.jobid", isRequired = true),
            @Param(name = "jobHandlerClassName", type = ApiParamType.STRING, desc = "nmtas.jobtestapi.input.param.desc.jobhandlerclassname", isRequired = true)
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String jobUuid = paramObj.getString("jobUuid");
        String jobHandlerClassName = paramObj.getString("jobHandlerClassName");
        IScheduleCrossoverService scheduleCrossoverService = CrossoverServiceFactory.getApi(IScheduleCrossoverService.class);
        scheduleCrossoverService.scheduleTest(jobHandlerClassName, jobUuid, "public");
        return null;
    }

    @Override
    public String getToken() {
        return "job/test";
    }
}
