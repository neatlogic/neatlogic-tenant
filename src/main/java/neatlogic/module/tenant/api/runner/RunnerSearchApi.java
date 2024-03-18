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
import neatlogic.framework.exception.runner.RunnerGroupIdNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = RUNNER_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class RunnerSearchApi extends PrivateApiComponentBase {

    @Resource
    RunnerMapper runnerMapper;

    @Override
    public String getName() {
        return "查询runner列表";
    }

    @Override
    public String getToken() {
        return "runner/search";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "groupId", type = ApiParamType.LONG, desc = "runner组id"),
            @Param(name = "isFilterGroup", type = ApiParamType.INTEGER, desc = "是否过滤runner组"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字（ip或者名称）"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否分页"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条数")
    })
    @Output({
            @Param(name = "tbodyList", desc = "runner列表")
    })
    @Description(desc = "用于runner管理页面的查询和runner组管理页面的runner列表查询")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long groupId = paramObj.getLong("groupId");
        RunnerVo runnerVo = JSONObject.toJavaObject(paramObj, RunnerVo.class);
        runnerVo.setIsDelete(0);
        int rowNum = 0;
        if (groupId != null) {
            if (runnerMapper.checkRunnerGroupIdIsExist(groupId) == 0) {
                throw new RunnerGroupIdNotFoundException(groupId);
            }
            rowNum = runnerMapper.searchRunnerCountByGroupId(groupId);
        } else {
            rowNum = runnerMapper.searchRunnerCount(runnerVo);
        }
        runnerVo.setRowNum(rowNum);
        return TableResultUtil.getResult(runnerMapper.searchRunner(runnerVo), runnerVo);


    }
}
