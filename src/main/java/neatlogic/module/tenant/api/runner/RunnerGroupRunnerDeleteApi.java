/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

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

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.RUNNER_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.runner.RunnerMapper;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
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
