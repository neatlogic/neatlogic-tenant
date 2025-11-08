/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.api.team;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.exception.team.TeamNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = NoAuth.class)
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
