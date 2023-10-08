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

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.RUNNER_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.runner.RunnerAuthVo;
import neatlogic.framework.dto.runner.RunnerVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.service.RunnerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
@AuthAction(action = RUNNER_MODIFY.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class RunnerSaveApi extends PrivateApiComponentBase {

    @Resource
    RunnerService runnerService;

    @Override
    public String getName() {
        return "nmtar.runnersaveapi.getname";
    }

    @Override
    public String getToken() {
        return "runner/save";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, isRequired = false, desc = "runner id"),
            @Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "runner name"),
            @Param(name = "protocol", type = ApiParamType.ENUM, isRequired = true, rule = "http,https", desc = "term.cmdb.protocol"),
            @Param(name = "host", type = ApiParamType.STRING, desc = "runner host", xss = true),
            @Param(name = "nettyPort", type = ApiParamType.INTEGER, desc = "nmtar.runnerregisterapi.input.param.nettyport"),
            @Param(name = "port", type = ApiParamType.INTEGER, desc = "nmtar.runnerregisterapi.input.param.port"),
            @Param(name = "isAuth", type = ApiParamType.INTEGER, desc = "nmtar.runnersaveapi.input.param.isauth"),
            @Param(name = "runnerAuthList", explode = RunnerAuthVo.class, type = ApiParamType.JSONARRAY, desc = "nmtar.runnersaveapi.input.param.authlist"),
    })
    @Output({
    })
    @Description(desc = "runner 保存接口,直接由ip确定一个runner")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        RunnerVo paramRunner = JSONObject.toJavaObject(paramObj, RunnerVo.class);
        Long id = paramObj.getLong("id");
        runnerService.SaveRunner(paramRunner, id);
        return null;
    }
}
