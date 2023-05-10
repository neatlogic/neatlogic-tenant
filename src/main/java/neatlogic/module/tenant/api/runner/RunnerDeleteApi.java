/*
Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package neatlogic.module.tenant.api.runner;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.RUNNER_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.runner.RunnerMapper;
import neatlogic.framework.dto.runner.RunnerVo;
import neatlogic.framework.exception.runner.RunnerIdNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
@AuthAction(action = RUNNER_MODIFY.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class RunnerDeleteApi extends PrivateApiComponentBase {

    @Resource
    RunnerMapper runnerMapper;

    @Override
    public String getName() {
        return "删除runner";
    }

    @Override
    public String getToken() {
        return "runner/delete";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "runner id")
    })
    @Output({
    })
    @Description(desc = "用于runner管理页面，runner删除接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {

        Long id = paramObj.getLong("id");
        if (runnerMapper.checkRunnerIdIsExist(id) == 0) {
            throw new RunnerIdNotFoundException(id);
        }

        RunnerVo runnerVo = runnerMapper.getRunnerById(id);
        runnerVo.setIsDelete(1);
        runnerMapper.updateRunner(runnerVo);
        runnerMapper.deleteRunnerGroupRunnerByRunnerId(id);
        return null;
    }

}
