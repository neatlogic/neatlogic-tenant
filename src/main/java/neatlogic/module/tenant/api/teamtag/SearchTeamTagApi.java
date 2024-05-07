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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.TEAM_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.teamtag.TeamTagMapper;
import neatlogic.framework.dto.teamtag.TeamTagVo;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@AuthAction(action = TEAM_MODIFY.class)
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchTeamTagApi extends PrivateApiComponentBase {
    
    @Resource
    TeamTagMapper teamTagMapper;
    
    @Override
    public String getName() {
        return "nmtat.searchteamtagapi.getname";
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword", xss = true, help = "名称"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize"),
            @Param(name = "defaultValue", type = ApiParamType.JSONARRAY, desc = "common.defaultvalue")
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        TeamTagVo teamTag = JSON.toJavaObject(paramObj, TeamTagVo.class);
        JSONArray defaultValue = teamTag.getDefaultValue();
        List<TeamTagVo> teamTagList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(defaultValue)) {
            List<Long> idList = defaultValue.toJavaList(Long.class);
            teamTagList = teamTagMapper.getTeamTagListByIdList(idList);
            return TableResultUtil.getResult(teamTagList);
        } else {
            int rowNum = teamTagMapper.searchTeamTagCount(teamTag);
            teamTag.setRowNum(rowNum);
            if (rowNum > 0) {
                teamTagList = teamTagMapper.searchTeamTag(teamTag);
            }
        }
        return TableResultUtil.getResult(teamTagList, teamTag);
    }

    @Override
    public String getToken() {
        return "team/tag/search";
    }
}
