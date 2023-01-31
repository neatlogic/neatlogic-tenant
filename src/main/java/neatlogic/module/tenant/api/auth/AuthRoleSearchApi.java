/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.tenant.api.auth;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dto.RoleVo;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: neatlogic
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
        List<RoleVo> roleList = roleMapper.getRoleListByAuthName(auth);
        returnObj.put("roleList", roleList);
        returnObj.put("roleCount", roleList.size());
        return returnObj;
    }
}
