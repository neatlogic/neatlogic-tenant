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

package neatlogic.module.tenant.api.runner;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.RUNNER_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.runner.RunnerMapper;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = RUNNER_MODIFY.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class RunnerGroupRunnerDeleteApi extends PrivateApiComponentBase {

    @Resource
    RunnerMapper runnerMapper;

    @Override
    public String getName() {
        return "删除runner组和runner的关系";
    }

    @Override
    public String getToken() {
        return "runnergroup/runner/delete";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "groupId", type = ApiParamType.LONG, isRequired = true, desc = "groupId"),
            @Param(name = "runnerId", type = ApiParamType.LONG, isRequired = true, desc = "runnerId")
    })
    @Output({
    })
    @Description(desc = "用于runner组管理页面关联的runner页面，runner删除接口（删除runner组和runner的关系）")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long groupId = paramObj.getLong("groupId");
        Long runnerId = paramObj.getLong("runnerId");
        runnerMapper.deleteRunnerGroupRunnerByGroupIdAndRunnerId(groupId,runnerId);
        return null;
    }

}
