package neatlogic.module.tenant.api.role;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ROLE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.*;
import neatlogic.framework.exception.role.RoleNameRepeatException;
import neatlogic.framework.exception.role.RoleNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@AuthAction(action = ROLE_MODIFY.class)
@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class RoleSaveApi extends PrivateApiComponentBase {

    @Resource
    RoleMapper roleMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public String getToken() {
        return "role/save";
    }

    @Override
    public String getName() {
        return "nmtar.rolesaveapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid",
                    type = ApiParamType.STRING,
                    desc = "common.uuid"),
            @Param(name = "name",
                    type = ApiParamType.STRING,
                    desc = "common.name",
                    isRequired = true, xss = true),
            @Param(name = "description",
                    type = ApiParamType.STRING,
                    desc = "common.description", xss = true),
            @Param(name = "rule",
                    type = ApiParamType.STRING,
                    desc = "common.rule",
                    help = "登录认证的请求需要携带Header做规则表达式，如果表达式执行后的值为true则该角色生效，否则不生效\\n如： ${DATA.env} == \"sit\" and ${DATA.test} == \"1\""),
            @Param(name = "userUuidList",
                    type = ApiParamType.JSONARRAY,
                    desc = "common.useruuidlist"),
            @Param(name = "teamList",
                    type = ApiParamType.JSONARRAY,
                    desc = "common.teamlist，[{\"uuid\":\"aaaaaaaaaa\", \"checkedChildren\":1}]"),
            @Param(name = "roleAuthList",
                    desc = "common.roleauthlist",
                    type = ApiParamType.JSONOBJECT)})
    @Output({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "common.uuid")
    })
    @Description(desc = "nmtar.rolesaveapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        RoleVo roleVo = new RoleVo();
        roleVo.setName(jsonObj.getString("name"));
        roleVo.setDescription(jsonObj.getString("description"));
        roleVo.setRule(jsonObj.getString("rule"));
        String uuid = jsonObj.getString("uuid");
        if (StringUtils.isNotBlank(uuid)) {
            if (roleMapper.checkRoleIsExists(uuid) == 0) {
                throw new RoleNotFoundException(uuid);
            }
            if (roleMapper.checkRoleNameIsIsRepeat(roleVo) > 0) {
                throw new RoleNameRepeatException(roleVo.getName());
            }
            roleVo.setUuid(uuid);
            roleMapper.updateRole(roleVo);
        } else {
            if (roleMapper.checkRoleNameIsIsRepeat(roleVo) > 0) {
                throw new RoleNameRepeatException(roleVo.getName());
            }
            RoleVo oldRole = roleMapper.getRoleByName(roleVo.getName());
            if (oldRole != null) {
                roleVo.setId(oldRole.getId());
                roleVo.setUuid(oldRole.getUuid());
                roleVo.setIsDelete(0);
                roleMapper.updateRole(roleVo);
            } else {
                roleMapper.insertRole(roleVo);
            }
            JSONArray userUuidArray = jsonObj.getJSONArray("userUuidList");
            if (CollectionUtils.isNotEmpty(userUuidArray)) {
                List<String> userUuidList = userUuidArray.toJavaList(String.class);
                List<String> existUserUuidList = userMapper.getUserUuidListByUuidListAndIsActive(userUuidList, 1);
                if (CollectionUtils.isNotEmpty(existUserUuidList)) {
                    for (String userUuid : existUserUuidList) {
                        roleMapper.replaceRoleUser(new RoleUserVo(roleVo.getUuid(), userUuid));
                    }
                }
            }
            JSONArray teamList = jsonObj.getJSONArray("teamList");
            if (CollectionUtils.isNotEmpty(teamList)) {
                List<RoleTeamVo> roleTeamList = new ArrayList<>(100);
                for (int i = 0; i < teamList.size(); i++) {
                    JSONObject team = teamList.getJSONObject(i);
                    if (team != null) {
                        roleTeamList.add(new RoleTeamVo(roleVo.getUuid(), team.getString("uuid"), team.getInteger("checkedChildren")));
                        if (roleTeamList.size() >= 100) {
                            roleMapper.insertRoleTeamList(roleTeamList);
                            roleTeamList.clear();
                        }
                    }
                }
                if (CollectionUtils.isNotEmpty(roleTeamList)) {
                    roleMapper.insertRoleTeamList(roleTeamList);
                }
            }

            JSONObject roleAuthObj = jsonObj.getJSONObject("roleAuthList");
            if (MapUtils.isNotEmpty(roleAuthObj)) {
                RoleAuthVo roleAuthVo = new RoleAuthVo();
                roleAuthVo.setRoleUuid(roleVo.getUuid());
                Set<String> keySet = roleAuthObj.keySet();
                for (String key : keySet) {
                    roleAuthVo.setAuthGroup(key);
                    JSONArray roleAuthArray = roleAuthObj.getJSONArray(key);
                    for (int j = 0; j < roleAuthArray.size(); j++) {
                        roleAuthVo.setAuth(roleAuthArray.getString(j));
                        roleMapper.insertRoleAuth(roleAuthVo);
                    }
                }
            }
        }
        resultObj.put("uuid", roleVo.getUuid());
        return resultObj;
    }

    public IValid name() {
        return value -> {
            RoleVo roleVo = JSON.toJavaObject(value, RoleVo.class);
            if (roleMapper.checkRoleNameIsIsRepeat(roleVo) > 0) {
                return new FieldValidResultVo(new RoleNameRepeatException(roleVo.getName()));
            }
            return new FieldValidResultVo();
        };
    }
}
