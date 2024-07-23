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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.USER_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.dto.UserAuthVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.exception.user.UserIdRepeatException;
import neatlogic.framework.exception.user.UserNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.service.UserService;
import neatlogic.framework.util.Md5Util;
import neatlogic.framework.util.UuidUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
@AuthAction(action = USER_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class UserSaveApi extends PrivateApiComponentBase {

    @Resource
    UserMapper userMapper;

    @Resource
    UserService userService;


    @Override
    public String getToken() {
        return "user/save";
    }

    @Override
    public String getName() {
        return "nmtau.usersaveapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "common.uuid"),
            @Param(name = "userId", type = ApiParamType.STRING, desc = "common.userid", isRequired = true, xss = true),
            @Param(name = "userName", type = ApiParamType.STRING, desc = "common.username", isRequired = true, xss = true),
            @Param(name = "password", type = ApiParamType.STRING, desc = "common.password", xss = true),
            @Param(name = "email", type = ApiParamType.STRING, desc = "common.email", isRequired = false, xss = true),
            @Param(name = "phone", type = ApiParamType.STRING, desc = "common.phone", isRequired = false, xss = true),
            @Param(name = "isActive", type = ApiParamType.INTEGER, desc = "common.isactive", isRequired = false),
            @Param(name = "teamUuidList", type = ApiParamType.JSONARRAY, desc = "common.teamuuidlist", isRequired = false),
            @Param(name = "roleUuidList", type = ApiParamType.JSONARRAY, desc = "common.roleuuidlist", isRequired = false),
            @Param(name = "userInfo", type = ApiParamType.STRING, desc = "term.framework.userotherinfo", isRequired = false, xss = true),
            @Param(name = "vipLevel", type = ApiParamType.ENUM, desc = "term.framework.viplevel", isRequired = false, rule = "0,1,2,3,4,5"),
            @Param(name = "userAuthList", type = ApiParamType.JSONOBJECT, desc = "common.authlist")
    })
    @Output({})
    @Description(desc = "nmtau.usersaveapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        UserVo userVo = new UserVo();
        userVo.setUserId(jsonObj.getString("userId"));
        userVo.setUserName(jsonObj.getString("userName"));
        userVo.setPassword(jsonObj.getString("password"));
        userVo.setEmail(jsonObj.getString("email"));
        userVo.setPhone(jsonObj.getString("phone"));
        userVo.setIsActive(jsonObj.getInteger("isActive"));
        userVo.setUserInfo(jsonObj.getString("userInfo"));
        userVo.setVipLevel(jsonObj.getInteger("vipLevel"));

        String uuid = jsonObj.getString("uuid");
        if (StringUtils.isBlank(uuid)) {
            userVo.setUuid(UuidUtil.randomUuid());
            if (userMapper.checkUserIdIsIsRepeat(userVo) > 0) {
                throw new UserIdRepeatException(userVo.getUserId());
            }
            UserVo deletedUserVo = userMapper.getUserByUserId(userVo.getUserId());
            if (deletedUserVo != null) {
                // userId是已删除的旧用户，重用用户uuid
                String oldUuid = deletedUserVo.getUuid();
                userMapper.updateUserIsNotDeletedByUuid(oldUuid);
                userVo.setUuid(oldUuid);
                // 删除用户角色
                userMapper.deleteUserRoleByUserUuid(oldUuid);
                // 删除用户组
                userMapper.deleteUserTeamByUserUuid(oldUuid);
                // 删除用户权限
                userMapper.deleteUserAuth(new UserAuthVo(oldUuid));
                // 删除用户密码
                userMapper.deleteUserPasswordByUserUuid(oldUuid);
                // 删除用户个性化中弹窗提醒设置数据
                userMapper.deleteUserDataByUserUuid(oldUuid);
                // 删除用户个性化中默认模块及各模块首页设置数据
                userMapper.deleteUserProfileByUserUuidAndModuleId(oldUuid, null);
                userMapper.updateUser(userVo);
            } else {
                userMapper.insertUser(userVo);
            }
            userMapper.insertUserPassword(userVo);
            JSONObject userAuthObj = jsonObj.getJSONObject("userAuthList");
            if (MapUtils.isNotEmpty(userAuthObj)) {
                Set<String> keySet = userAuthObj.keySet();
                for (String key : keySet) {
                    JSONArray authArray = userAuthObj.getJSONArray(key);
                    for (int j = 0; j < authArray.size(); j++) {
                        UserAuthVo authVo = new UserAuthVo();
                        authVo.setAuth(authArray.getString(j));
                        authVo.setAuthGroup(key);
                        authVo.setUserUuid(userVo.getUuid());
                        userMapper.insertUserAuth(authVo);
                    }
                }
            }
            // 自动生成token
            String token = Md5Util.encryptMD5(UUID.randomUUID().toString());
            userMapper.updateUserTokenByUuid(token, userVo.getUuid());
        } else {
            UserVo existUserVo = userMapper.getUserBaseInfoByUuid(uuid);
            if (existUserVo == null || Objects.equals(existUserVo.getIsDelete(), 1)) {
                throw new UserNotFoundException(uuid);
            }
            userVo.setUuid(uuid);
            userMapper.updateUser(userVo);
            // 删除用户角色
            userMapper.deleteUserRoleByUserUuid(userVo.getUuid());
            // 删除用户组
            userMapper.deleteUserTeamByUserUuid(userVo.getUuid());
            //更新密码
            if (StringUtils.isNotBlank(userVo.getPassword())) {
                userMapper.updateUserPasswordActive(userVo.getUuid());
                List<Long> idList = userMapper.getLimitUserPasswordIdList(userVo.getUuid());
                if (CollectionUtils.isNotEmpty(idList)) {
                    userMapper.deleteUserPasswordByLimit(userVo.getUuid(), idList);
                }
                userMapper.insertUserPassword(userVo);
            }
        }

        JSONArray teamUuidArray = jsonObj.getJSONArray("teamUuidList");
        if (CollectionUtils.isNotEmpty(teamUuidArray)) {
            List<String> teamUuidList = teamUuidArray.toJavaList(String.class);
            for (String teamUuid : teamUuidList) {
                userMapper.insertUserTeam(userVo.getUuid(), teamUuid.replaceAll(GroupSearch.TEAM.getValuePlugin(), StringUtils.EMPTY));
            }
        }

        JSONArray roleUuidArray = jsonObj.getJSONArray("roleUuidList");
        if (CollectionUtils.isNotEmpty(roleUuidArray)) {
            List<String> roleUuidList = roleUuidArray.toJavaList(String.class);
            for (String roleUuid : roleUuidList) {
                userMapper.insertUserRole(userVo.getUuid(), roleUuid.replaceAll(GroupSearch.ROLE.getValuePlugin(), StringUtils.EMPTY));
            }
        }

        userService.updateUserCacheAndSessionByUserUuid(userVo.getUuid());

        return userVo.getUuid();
    }

    public IValid userId() {
        return value -> {
            UserVo userVo = JSON.toJavaObject(value, UserVo.class);
            if (StringUtils.isBlank(userVo.getUuid())) {
                userVo.setUuid(UuidUtil.randomUuid());
            }
            if (userMapper.checkUserIdIsIsRepeat(userVo) > 0) {
                return new FieldValidResultVo(new UserIdRepeatException(userVo.getUserId()));
            }
            return new FieldValidResultVo();
        };
    }
}
