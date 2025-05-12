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

package neatlogic.module.tenant.api.user;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.RoleVo;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.exception.user.UserNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class UserGetForEditApi extends PrivateApiComponentBase {

    @Resource
    private UserMapper userMapper;

    @Resource
    private TeamMapper teamMapper;

    @Resource
    private RoleMapper roleMapper;

    @Override
    public String getToken() {
        return "user/get/foredit";
    }

    @Override
    public String getName() {
        return "获取用户信息(编辑页面回显专用)";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "userUuid", type = ApiParamType.STRING, isRequired = true, desc = "common.useruuid")
    })
    @Output({
            @Param(name = "Return", explode = UserVo.class, desc = "nmtau.usergetapi.output.param.desc.user")
    })
    @Description(desc = "获取用户信息(编辑页面回显专用)")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String userUuid = jsonObj.getString("userUuid");
        UserVo userVo = userMapper.getUserByUuid(userUuid);
        if (userVo == null) {
            throw new UserNotFoundException(userUuid);
        }
        JSONObject userObj = new JSONObject();
        userObj.put("id", userVo.getId());
        userObj.put("uuid", userVo.getUuid());
        userObj.put("userId", userVo.getUserId());
        userObj.put("userName", userVo.getUserName());
        userObj.put("vipLevel", userVo.getVipLevel());
        userObj.put("email", userVo.getEmail());
        userObj.put("phone", userVo.getPhone());
        userObj.put("isActive", userVo.getIsActive());
        /**
         * 补充分组角色信息,以用户的a分组为例
         * 1、根据a分组的父节点（需要穿透）找到roleList
         * 2、根据a分组找到roleList
         * 3、将以上两点找到的roleList 以role的uuid为唯一键合并
         */
        List<String> teamUuidList = new ArrayList<>();
        List<RoleVo> teamRoleList = new ArrayList<>();
        List<TeamVo> teamList = teamMapper.getTeamListByUserUuid(userUuid);
        for (TeamVo teamVo : teamList) {
            teamUuidList.add(teamVo.getUuid());
            List<RoleVo> list = roleMapper.getParentTeamRoleListWithCheckedChildrenByTeam(teamVo);
            teamRoleList.addAll(list);
        }
        if (CollectionUtils.isNotEmpty(teamUuidList)) {
            List<RoleVo> list = roleMapper.getRoleListWithTeamByTeamUuidList(teamUuidList);
            teamRoleList.addAll(list);
        }
        Map<String, RoleVo> roleVoMap = new HashMap<>();
        for (RoleVo roleVo : teamRoleList) {
            String uuid = roleVo.getUuid();
            if (!roleVoMap.containsKey(uuid)) {
                roleVoMap.put(uuid, roleVo);
            } else {
                roleVoMap.get(uuid).getTeamList().addAll(roleVo.getTeamList());
            }
        }
        userObj.put("teamRoleList", new ArrayList<>(roleVoMap.values()));
        List<String> newTeamUuidList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(teamUuidList)) {
            for (String teamUuid : teamUuidList) {
                newTeamUuidList.add(GroupSearch.TEAM.addPrefix(teamUuid));
            }
        }
        userObj.put("teamUuidList", newTeamUuidList);
        List<String> roleUuidList = roleMapper.getRoleUuidListByUserUuid(userUuid);
        List<String> newRoleUuidList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(roleUuidList)) {
            for (String roleUuid : roleUuidList) {
                newRoleUuidList.add(GroupSearch.ROLE.addPrefix(roleUuid));
            }
        }
        userObj.put("roleUuidList", newRoleUuidList);
        return userObj;
    }
}
