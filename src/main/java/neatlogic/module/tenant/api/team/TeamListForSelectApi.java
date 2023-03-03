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

package neatlogic.module.tenant.api.team;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class TeamListForSelectApi extends PrivateApiComponentBase {

    @Autowired
    private TeamMapper teamMapper;

    @Override
    public String getToken() {
        return "team/list/forselect";
    }

    @Override
    public String getName() {
        return "分组查询接口（下拉框）";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字", xss = true),
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "分组uuid"),
            @Param(name = "parentUuid", type = ApiParamType.STRING, desc = "父分组uuid"),
            @Param(name = "level", type = ApiParamType.STRING, desc = "级别"),
            @Param(name = "defaultValue", type = ApiParamType.JSONARRAY, desc = "用于回显的参数列表"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数据条目"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true")
    })
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.STRING, explode = TeamVo[].class, desc = "分组信息"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "分组查询接口（下拉框）")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        TeamVo teamVo = JSONObject.toJavaObject(jsonObj, TeamVo.class);
        JSONObject returnObj = new JSONObject();
        JSONArray defaultValue = teamVo.getDefaultValue();
        if (CollectionUtils.isNotEmpty(defaultValue)) {
            List<TeamVo> teamList = teamMapper.getTeamByUuidList(defaultValue.toJavaList(String.class));
            returnObj.put("tbodyList", teamList);
        } else {
            teamVo.setIsDelete(0);
            int rowNum = teamMapper.searchTeamCount(teamVo);
            teamVo.setRowNum(rowNum);
            returnObj.put("pageSize", teamVo.getPageSize());
            returnObj.put("currentPage", teamVo.getCurrentPage());
            returnObj.put("rowNum", rowNum);
            returnObj.put("pageCount", teamVo.getPageCount());
            if (rowNum > 0 && teamVo.getCurrentPage() <= teamVo.getPageCount()) {
                List<TeamVo> teamList = teamMapper.searchTeamOrderByNameLengthForSelect(teamVo);
                returnObj.put("tbodyList", teamList);
            } else {
                returnObj.put("tbodyList", new ArrayList<>());
            }
        }
        return returnObj;
    }

}
