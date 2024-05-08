/*
 * Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neatlogic.module.tenant.api.teamtag;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.REGION_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.teamtag.TeamTagMapper;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.dto.teamtag.TeamTagTeamVo;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AuthAction(action = REGION_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchTeamTagTeamApi extends PrivateApiComponentBase {
    @Resource
    TeamTagMapper teamTagMapper;

    @Resource
    TeamMapper teamMapper;

    @Override
    public String getName() {
        return "查询标签映射分组";
    }


    @Input({
            @Param(name = "tagIdList", type = ApiParamType.JSONARRAY, desc = "common.tagidlist", isRequired = true, minSize = 1,help = "标签id列表"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize")
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        TeamTagTeamVo teamTagTeam = JSON.toJavaObject(paramObj, TeamTagTeamVo.class);
        List<TeamVo> teamList = new ArrayList<>();
        int rowNum = teamTagMapper.searchTeamTagTeamCount(teamTagTeam);
        teamTagTeam.setRowNum(rowNum);
        if (rowNum > 0) {
            List<TeamTagTeamVo> teamTagTeamList = teamTagMapper.getTeamTagTeamUuidList(teamTagTeam);
            Map<String,Integer> teamCheckedChildrenMap = teamTagTeamList.stream().collect(Collectors.toMap(TeamTagTeamVo::getTeamUuid, TeamTagTeamVo::getCheckedChildren));
            teamList = teamMapper.getTeamByUuidList(teamTagTeamList.stream().map(TeamTagTeamVo::getTeamUuid).collect(Collectors.toList()));
            teamList.forEach(t-> t.setCheckedChildren(teamCheckedChildrenMap.get(t.getUuid())));
        }

        return TableResultUtil.getResult(teamList, teamTagTeam);
    }

    @Override
    public String getToken() {
        return "team/tag/team/search";
    }
}
