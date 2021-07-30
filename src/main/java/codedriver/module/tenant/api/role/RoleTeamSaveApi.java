package codedriver.module.tenant.api.role;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.ROLE_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dto.RoleTeamVo;
import codedriver.framework.exception.role.RoleNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@AuthAction(action = ROLE_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class RoleTeamSaveApi extends PrivateApiComponentBase {

    @Resource
    private RoleMapper roleMapper;

    @Override
    public String getToken() {
        return "role/team/save";
    }

    @Override
    public String getName() {
        return "角色分组添加接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "roleUuid", type = ApiParamType.STRING, isRequired = true, desc = "角色uuid"),
            @Param(name = "teamUuidList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "分组Uuid集合")
    })
    @Description(desc = "角色分组添加接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String roleUuid = jsonObj.getString("roleUuid");
        if (roleMapper.checkRoleIsExists(roleUuid) == 0) {
            throw new RoleNotFoundException(roleUuid);
        }
        roleMapper.deleteTeamRoleByRoleUuid(roleUuid);
        JSONArray teamUuidArray = jsonObj.getJSONArray("teamUuidList");
        if (CollectionUtils.isNotEmpty(teamUuidArray)) {
            List<RoleTeamVo> roleTeamList = new ArrayList<>(100);
            List<String> teamUuidList = teamUuidArray.toJavaList(String.class);
            for (String teamUuid : teamUuidList) {
                roleTeamList.add(new RoleTeamVo(roleUuid, teamUuid));
                if (roleTeamList.size() >= 100) {
                    roleMapper.insertRoleTeamList(roleTeamList);
                    roleTeamList.clear();
                }
            }
            if (CollectionUtils.isNotEmpty(roleTeamList)) {
                roleMapper.insertRoleTeamList(roleTeamList);
            }
        }
        return null;
    }
}
