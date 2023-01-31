/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

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
