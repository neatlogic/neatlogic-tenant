package codedriver.module.tenant.api.role;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class RoleUserSearchApi extends PrivateApiComponentBase {

    @Resource
    private UserMapper userMapper;


    @Override
    public String getName() {
        return "角色用户查询接口";
    }

    @Override
    public String getToken() {
        return "role/user/search";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字(用户id或名称),模糊查询", xss = true),
            @Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", desc = "状态"),
            @Param(name = "vipLevel", type = ApiParamType.INTEGER, desc = "VIP等级"),
            @Param(name = "authGroup", type = ApiParamType.STRING, desc = "权限模块"),
            @Param(name = "auth", type = ApiParamType.STRING, desc = "权限"),
            @Param(name = "teamUuid", type = ApiParamType.STRING, desc = "用户组uuid"),
            @Param(name = "roleUuid", type = ApiParamType.STRING, desc = "角色uuid"),
    })

    @Output({
            @Param(name = "userCount", type = ApiParamType.STRING, desc = "角色用户总数"),
            @Param(name = "roleUserList", desc = "角色用户列表", type = ApiParamType.JSONARRAY, explode = UserVo[].class)
    })

    @Description(desc = "角色用户查询接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        UserVo userVo = JSON.toJavaObject(paramObj, UserVo.class);
        userVo.setIsDelete(0);
        resultObj.put("roleUserList", userMapper.searchRoleUser(userVo));
        resultObj.put("userCount", userMapper.searchUserCount(userVo));
        return resultObj;
    }

}
