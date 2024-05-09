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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.REGION_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.teamtag.TeamTagMapper;
import neatlogic.framework.dto.teamtag.TeamTagTeamVo;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@AuthAction(action = REGION_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
@Transactional
public class SaveTeamTagTeamApi extends PrivateApiComponentBase {
    @Resource
    TeamTagMapper teamTagMapper;

    @Override
    public String getName() {
        return "nmtat.saveteamtagteamapi.getname";
    }


    @Input({
            @Param(name = "tagIdList", type = ApiParamType.JSONARRAY, desc = "common.tagidlist", isRequired = true, help = "标签id列表"),
            @Param(name = "teamList", type = ApiParamType.JSONARRAY, desc = "common.teamlist", isRequired = true, help = "分组列表"),
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONArray tagIdList = paramObj.getJSONArray("tagIdList");
        JSONArray teamList = paramObj.getJSONArray("teamList");
        Long updateTime = System.currentTimeMillis();
        for (int i = 0; i < teamList.size(); i++) {
            JSONObject team = teamList.getJSONObject(i);
            if (MapUtils.isNotEmpty(team)) {
                String teamUuid = team.getString("uuid");
                Integer checkedChildren = team.getInteger("checkedChildren");
                TeamTagTeamVo teamTagTeamVo = new TeamTagTeamVo(tagIdList.toJavaList(Long.class), teamUuid, checkedChildren, updateTime);
                teamTagMapper.insertTeamTagTeam(teamTagTeamVo);
            }
        }
        //只有一个标签的时候才执行删除操作
        if(tagIdList.size() == 1) {
            teamTagMapper.deleteTeamTagTeamExpired(tagIdList.getLong(0), updateTime);
        }
        return null;
    }

    @Override
    public String getToken() {
        return "/team/tag/team/save";
    }
}
