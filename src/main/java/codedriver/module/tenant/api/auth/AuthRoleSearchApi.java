package codedriver.module.tenant.api.auth;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-13 12:03
 **/
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class AuthRoleSearchApi extends PrivateApiComponentBase {

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public String getToken() {
        return "auth/role/search";
    }

    @Override
    public String getName() {
        return "权限角色查询接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "auth", desc = "权限", type = ApiParamType.STRING, isRequired = true)
    })
    @Output({
            @Param(name = "roleList", desc = "角色集合", explode = RoleVo[].class, type = ApiParamType.JSONARRAY)
    })
    @Description(desc = "权限角色查询接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        String auth = jsonObj.getString("auth");
        RoleVo roleVo = new RoleVo();
        roleVo.setAuth(auth);
        roleVo.setNeedPage(false);
        List<RoleVo> roleList = roleMapper.searchRole(roleVo);
        returnObj.put("roleList", roleList);
        returnObj.put("roleCount", roleList.size());
        return returnObj;
    }
}
