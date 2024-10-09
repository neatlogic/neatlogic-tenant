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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.runner.RunnerMapper;
import neatlogic.framework.dto.runner.RunnerGroupVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class RunnerGroupSearchApi extends PrivateApiComponentBase {

    @Resource
    RunnerMapper runnerMapper;

    @Override
    public String getName() {
        return "查询runner组信息";
    }

    @Override
    public String getToken() {
        return "runnergroup/search";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Description(desc = "获取runner组列表")
    @Input({
            @Param(name = "defaultValue", type = ApiParamType.JSONARRAY, desc = "默认值"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键词"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否分页"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条数")
    })
    @Output({
            @Param(name = "tbodyList", explode = RunnerGroupVo[].class, desc = "所有runner组")
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        RunnerGroupVo groupVo = JSON.toJavaObject(paramObj, RunnerGroupVo.class);
        int rowNum = runnerMapper.searchRunnerGroupCount(groupVo);
        List<RunnerGroupVo> runnerGroupVoList = new ArrayList<>();
        groupVo.setRowNum(rowNum);
        if (rowNum > 0) {
            runnerGroupVoList = runnerMapper.searchRunnerGroup(groupVo);
        }
        return TableResultUtil.getResult(runnerGroupVoList, groupVo);
    }


}
