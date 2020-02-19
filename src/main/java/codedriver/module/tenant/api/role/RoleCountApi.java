package codedriver.module.tenant.api.role;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleCountApi extends ApiComponentBase {

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public String getToken() {
        return "role/count";
    }

    @Override
    public String getName() {
        return "角色统计接口";
    }

    @Override
    public String getConfig() {
        return null;
    }
    @Output({
            @Param(name = "roleCount", type = ApiParamType.INTEGER, desc = "角色数目")
    })
    @Description(desc = "角色统计接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        int count = roleMapper.searchRoleCount(new RoleVo());
        returnObj.put("roleCount", count);
        return returnObj;
    }
}