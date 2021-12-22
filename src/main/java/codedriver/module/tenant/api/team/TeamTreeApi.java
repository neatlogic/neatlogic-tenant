/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.team;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dto.RoleTeamVo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.exception.team.TeamNotFoundException;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class TeamTreeApi extends PrivateApiComponentBase {

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public String getToken() {
        return "team/tree";
    }

    @Override
    public String getName() {
        return "查询分组架构树接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "parentUuid", desc = "teamUuid，这里指父级uuid", type = ApiParamType.STRING),
            @Param(name = "isActive", desc = "是否只统计激活的用户", type = ApiParamType.INTEGER),
            @Param(name = "roleUuid", desc = "角色uuid", type = ApiParamType.STRING),
            @Param(name = "currentPage", desc = "当前页", type = ApiParamType.INTEGER),
            @Param(name = "needPage", desc = "是否分页", type = ApiParamType.BOOLEAN),
            @Param(name = "pageSize", desc = "每页最大数", type = ApiParamType.INTEGER)
    })
    @Output({
            @Param(name = "tbodyList", explode = TeamVo[].class, desc = "用户组织架构集合"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "查询分组架构树接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        TeamVo teamVo = new TeamVo();
        Boolean needPage = jsonObj.getBoolean("needPage");
        if (needPage != null) {
            teamVo.setNeedPage(needPage);
        }
        teamVo.setCurrentPage(jsonObj.getInteger("currentPage"));
        teamVo.setPageSize(jsonObj.getInteger("pageSize"));
        String parentUuid = jsonObj.getString("parentUuid");
        if (StringUtils.isNotBlank(parentUuid)) {
            if (teamMapper.checkTeamIsExists(parentUuid) == 0) {
                throw new TeamNotFoundException(parentUuid);
            }
        } else {
            parentUuid = TeamVo.ROOT_UUID;
        }
        teamVo.setParentUuid(parentUuid);
        teamVo.setIsDelete(0);
        int rowNum = teamMapper.searchTeamCount(teamVo);
        if (rowNum > 0) {
            if (teamVo.getNeedPage()) {
                teamVo.setRowNum(rowNum);
                returnObj.put("currentPage", teamVo.getCurrentPage());
                returnObj.put("pageCount", teamVo.getPageCount());
                returnObj.put("pageSize", teamVo.getPageSize());
                returnObj.put("rowNum", rowNum);
            }
            List<TeamVo> tbodyList = teamMapper.searchTeam(teamVo);
            /** 查出分组用户数量和子分组数量 **/
            if (CollectionUtils.isNotEmpty(tbodyList)) {
                Integer isActive = jsonObj.getInteger("isActive");
                List<String> teamUuidList = tbodyList.stream().map(TeamVo::getUuid).collect(Collectors.toList());
//                List<TeamVo> teamUserCountAndChildCountList = teamMapper.getTeamUserCountAndChildCountListByUuidList(teamUuidList, isActive);
//                Map<String, TeamVo> teamUserCountAndChildCountMap = new HashMap<>();
//                for (TeamVo team : teamUserCountAndChildCountList) {
//                    teamUserCountAndChildCountMap.put(team.getUuid(), team);
//                }
                List<TeamVo> childCountList = teamMapper.getChildCountListByUuidList(teamUuidList);
                Map<String, Integer> childCountMap = new HashMap<>();
                for (TeamVo team : childCountList) {
                    childCountMap.put(team.getUuid(), team.getChildCount());
                }
                List<TeamVo> teamUserCountList = teamMapper.getTeamUserCountListByUuidList(teamUuidList, isActive);
                Map<String, Integer> teamUserCountMap = new HashMap<>();
                for (TeamVo team : teamUserCountList) {
                    teamUserCountMap.put(team.getUuid(), team.getUserCount());
                }

                Map<String, RoleTeamVo> roleTeamMap = new HashMap<>();
                String roleUuid = jsonObj.getString("roleUuid");
                if (StringUtils.isNotBlank(roleUuid)) {
                    List<RoleTeamVo> roleTeamList = roleMapper.getRoleTeamListByRoleUuidAndTeamUuidList(roleUuid, teamUuidList);
                    for (RoleTeamVo roleTeamVo : roleTeamList) {
                        roleTeamMap.put(roleTeamVo.getTeamUuid(), roleTeamVo);
                    }
                }

                for (TeamVo team : tbodyList) {
//                    TeamVo teamUserCountAndChildCount = teamUserCountAndChildCountMap.get(team.getUuid());
//                    if (teamUserCountAndChildCount != null) {
//                        team.setChildCount(teamUserCountAndChildCount.getChildCount());
//                        team.setUserCount(teamUserCountAndChildCount.getUserCount());
//                    }
                    team.setChildCount(childCountMap.get(team.getUuid()));
                    team.setUserCount(teamUserCountMap.get(team.getUuid()));
                    if (StringUtils.isNotBlank(roleUuid)) {
                        RoleTeamVo roleTeamVo = roleTeamMap.get(team.getUuid());
                        if (roleTeamVo != null) {
                            team.setChecked(1);
                            team.setCheckedChildren(roleTeamVo.getCheckedChildren());
                        }
                    }
                }
            }
            returnObj.put("tbodyList", tbodyList);
        }

        return returnObj;
    }
}
