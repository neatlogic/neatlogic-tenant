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
