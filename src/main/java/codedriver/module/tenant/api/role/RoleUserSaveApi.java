package codedriver.module.tenant.api.role;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.RoleUserVo;
import codedriver.framework.exception.role.RoleNotFoundException;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import codedriver.framework.auth.label.ROLE_MODIFY;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
@AuthAction(action = ROLE_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class RoleUserSaveApi extends PrivateApiComponentBase {

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public String getToken() {
        return "role/user/save";
    }

    @Override
    public String getName() {
        return "角色用户添加接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "roleUuid",
                    type = ApiParamType.STRING,
                    desc = "角色uuid",
                    isRequired = true),
            @Param(name = "userUuidList",
                    type = ApiParamType.JSONARRAY,
                    desc = "用户Uuid集合"
            ),
//            @Param(name = "teamUuidList",
//                    type = ApiParamType.JSONARRAY,
//                    desc = "分组Uuid集合"
//            )
    })
    @Description(desc = "角色用户添加接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String roleUuid = jsonObj.getString("roleUuid");
        if (roleMapper.checkRoleIsExists(roleUuid) == 0) {
            throw new RoleNotFoundException(roleUuid);
        }
//        List<String> userUuidList = JSON.parseArray(jsonObj.getString("userUuidList"), String.class);
//        List<String> teamUuidList = JSON.parseArray(jsonObj.getString("teamUuidList"), String.class);
//        Set<String> uuidList = userService.getUserUuidSetByUserUuidListAndTeamUuidList(userUuidList,teamUuidList);
//        if(CollectionUtils.isNotEmpty(uuidList)){
//            roleMapper.deleteRoleUser(new RoleUserVo(roleUuid));
//            for (String userUuid : uuidList){
//                roleMapper.insertRoleUser(new RoleUserVo(roleUuid,userUuid));
//            }
//        }
        JSONArray userUuidArray = jsonObj.getJSONArray("userUuidList");
        if (CollectionUtils.isNotEmpty(userUuidArray)) {
            List<String> userUuidList = userUuidArray.toJavaList(String.class);
            List<String> existUserUuidList = userMapper.getUserUuidListByUuidListAndIsActive(userUuidList, 1);
            if (CollectionUtils.isNotEmpty(existUserUuidList)) {
                for (String userUuid : existUserUuidList) {
                    roleMapper.replaceRoleUser(new RoleUserVo(roleUuid, userUuid));
                }
            }
        }
        return null;
    }
}
