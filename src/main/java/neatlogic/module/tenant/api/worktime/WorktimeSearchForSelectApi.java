/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.worktime;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.worktime.dao.mapper.WorktimeMapper;
import neatlogic.framework.worktime.dto.WorktimeVo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class WorktimeSearchForSelectApi extends PrivateApiComponentBase {

    @Resource
    private WorktimeMapper worktimeMapper;

    @Override
    public String getToken() {
        return "worktime/search/forselect";
    }

    @Override
    public String getName() {
        return "查询工作时间窗口列表_下拉框";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "模糊搜索", xss = true),
            @Param(name = "isActive", type = ApiParamType.ENUM, desc = "是否激活", rule = "0,1"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
    })
    @Output({
            @Param(name = "list", explode = ValueTextVo[].class, desc = "工作时间窗口列表")
    })
    @Description(desc = "查询工作时间窗口列表_下拉框")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        WorktimeVo worktimeVo = JSON.toJavaObject(jsonObj, WorktimeVo.class);
        JSONObject resultObj = new JSONObject();
        if (worktimeVo.getNeedPage()) {
            int rowNum = worktimeMapper.searchWorktimeCount(worktimeVo);
            int pageCount = PageUtil.getPageCount(rowNum, worktimeVo.getPageSize());
            worktimeVo.setPageCount(pageCount);
            worktimeVo.setRowNum(rowNum);
            resultObj.put("currentPage", worktimeVo.getCurrentPage());
            resultObj.put("pageSize", worktimeVo.getPageSize());
            resultObj.put("pageCount", pageCount);
            resultObj.put("rowNum", rowNum);
        }

        List<ValueTextVo> worktimeList = worktimeMapper.searchWorktimeListForSelect(worktimeVo);
        resultObj.put("list", worktimeList);
        return resultObj;
    }

}
