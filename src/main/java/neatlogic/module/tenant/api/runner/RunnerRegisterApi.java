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
import neatlogic.framework.asynchronization.threadlocal.RequestContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.runner.RunnerVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.ApiAnonymousAccessSupportEnum;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.service.RunnerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.OPERATE)
public class RunnerRegisterApi extends PrivateApiComponentBase {

    @Resource
    RunnerService runnerService;

    @Override
    public String getName() {
        return "nmtar.runnerregisterapi.getname";
    }

    @Override
    public String getToken() {
        return "runner/register";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "protocol", type = ApiParamType.ENUM, isRequired = true, rule = "http,https", desc = "term.cmdb.protocol"),
            @Param(name = "nettyPort", type = ApiParamType.INTEGER, desc = "nmtar.runnerregisterapi.input.param.nettyport"),
            @Param(name = "port", type = ApiParamType.INTEGER, desc = "nmtar.runnerregisterapi.input.param.port")
    })
    @Output({
    })
    @Description(desc = "nmtar.runnerregisterapi.description.desc")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        RunnerVo paramRunner = JSONObject.toJavaObject(paramObj, RunnerVo.class);
        String host = RequestContext.get().getRequest().getRemoteHost();
        paramRunner.setHost(host);
        paramRunner.setName(host);
        runnerService.SaveRunner(paramRunner, null);
        return null;
    }

    @Override
    public ApiAnonymousAccessSupportEnum supportAnonymousAccess() {
        return ApiAnonymousAccessSupportEnum.ANONYMOUS_ACCESS_WITHOUT_ENCRYPTION;
    }
}
