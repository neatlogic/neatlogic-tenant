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

package neatlogic.module.tenant.service;

import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.TeamUserTitleVo;
import neatlogic.framework.dto.UserTitleVo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamServiceImpl implements TeamService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private TeamMapper teamMapper;

    @Override
    public  void deleteTeamUserTitleByTeamUuid(String teamUuid){
        List<TeamUserTitleVo> teamUserTitleVoList = teamMapper.getTeamUserTitleListByTeamUuid(teamUuid);
        teamMapper.deleteTeamUserTitleByTeamUuid(teamUuid);
        if(CollectionUtils.isNotEmpty(teamUserTitleVoList)) {
            List<UserTitleVo> userTitleVoList = userMapper.getUserTitleListLockByTitleNameList(teamUserTitleVoList.stream().map(TeamUserTitleVo::getTitle).collect(Collectors.toList()));
            for (UserTitleVo userTitleVo : userTitleVoList) {
                if (teamMapper.checkTitleIsReferenceByTitleId(userTitleVo.getId()) == 0) {
                    userMapper.deleteUserTitleByName(userTitleVo.getName());
                }
            }
        }
    }
}
