package codedriver.module.tenant.api.role;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.exception.role.RoleNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-20 10:35
 **/
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class RoleGetListApi extends PrivateApiComponentBase {

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public String getToken() {
        return "role/get/list";
    }

    @Override
    public String getName() {
        return "批量获取角色信息接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param( name = "roleUuidList", desc = "角色uuid集合", type = ApiParamType.JSONARRAY, isRequired = true)
    })
    @Output({
            @Param( name = "roleList", desc = "角色集合", explode = RoleVo[].class)
    })
    @Description(desc = "批量获取角色信息接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        List<String> roleUuidList = JSON.parseArray(jsonObj.getString("roleUuidList"), String.class);
        if (CollectionUtils.isNotEmpty(roleUuidList)){
            List<RoleVo> roleList = new ArrayList<>();
        	for (String roleUuid : roleUuidList){
        		RoleVo roleVo = roleMapper.getRoleByUuid(roleUuid);
        		if(roleVo == null) {
        			throw new RoleNotFoundException(roleUuid);
        		}
        		int userCount = roleMapper.searchRoleUserCountByRoleUuid(roleUuid);
        		roleVo.setUserCount(userCount);
                roleList.add(roleVo);
        	}
        	returnObj.put("roleList", roleList);
        }
        return returnObj;
    }
}
