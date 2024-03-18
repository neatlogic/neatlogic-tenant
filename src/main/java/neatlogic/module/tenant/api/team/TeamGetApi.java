/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

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

package neatlogic.module.tenant.api.team;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.exception.team.TeamNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class TeamGetApi extends PrivateApiComponentBase {

    @Autowired
    private TeamMapper teamMapper;

    @Override
    public String getToken() {
        return "team/get";
    }

    @Override
    public String getName() {
        return "获取组信息接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid",
                    type = ApiParamType.STRING,
                    desc = "分组uuid",
                    minLength = 32,
                    maxLength = 32,
                    isRequired = true),
            @Param(name = "name",
                    type = ApiParamType.STRING,
                    desc = "分组名称"),
            @Param(name = "isEdit",
                    type = ApiParamType.INTEGER,
                    desc = "是否edit,0 为添加下级分组，1为编辑,",
                    isRequired = true)
    })
    @Output({
            @Param(name = "teamVo",
                    explode = TeamVo.class,
                    desc = "组id")})
    @Description(desc = "获取组信息接口")
    @Override
    public Object myDoService(JSONObject jsonObj) {
        String uuid = jsonObj.getString("uuid");
        TeamVo team = new TeamVo();
        team.setName(jsonObj.getString("name"));
        team.setUuid(uuid);
        TeamVo teamVo = teamMapper.getTeam(team);
        if (teamVo == null) {
            throw new TeamNotFoundException(uuid);
        }
        int userCount = teamMapper.searchUserCountByTeamUuid(team.getUuid());
        teamVo.setUserCount(userCount);
        int isEdit = jsonObj.getIntValue("isEdit");
        List<String> pathNameList = new ArrayList<>();
        String upwardNamePath = teamVo.getUpwardNamePath();
        if (StringUtils.isNotBlank(upwardNamePath)) {
            String[] upwardNameArray = upwardNamePath.split("/");
            for (String upwardName : upwardNameArray) {
                if (isEdit == 0 || !upwardName.equals(teamVo.getName())) {
                    pathNameList.add(upwardName);
                }
            }
        }
        teamVo.setPathNameList(pathNameList);
        teamVo.setTeamUserTitleList(teamMapper.getTeamUserTitleListByTeamUuid(uuid));
        return teamVo;
    }

}
