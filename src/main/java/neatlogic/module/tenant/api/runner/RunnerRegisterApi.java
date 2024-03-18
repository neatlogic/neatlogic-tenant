/*Copyright (C) 2023  深圳极向量科技有限公司 All Rights Reserved.

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
