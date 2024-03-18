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
