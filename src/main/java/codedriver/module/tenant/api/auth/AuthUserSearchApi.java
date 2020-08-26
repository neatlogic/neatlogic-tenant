package codedriver.module.tenant.api.auth;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-13 12:01
 **/
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class AuthUserSearchApi extends PrivateApiComponentBase {

    @Autowired
    private UserMapper userMapper;

    @Override
    public String getToken() {
        return "auth/user/search";
    }

    @Override
    public String getName() {
        return "权限用户查询接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input( {
            @Param( name = "auth",  desc = "权限", type = ApiParamType.STRING, isRequired = true)
    })

    @Output({
            @Param( name = "userList", desc = "用户列表", type = ApiParamType.JSONARRAY, explode = UserVo[].class),
            @Param( name = "roleUserList", desc = "角色用户列表", type = ApiParamType.JSONARRAY, explode = UserVo[].class)
    })

    @Description(desc = "权限用户查询接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        String auth = jsonObj.getString("auth");
        List<UserVo> roleUserList = userMapper.searchRoleUserByAuth(auth);
        Set<String> roleUserSet = new HashSet<>();
        if (CollectionUtils.isNotEmpty(roleUserList)){
            for (UserVo user : roleUserList){
                roleUserSet.add(user.getUuid());
            }
        }
        List<UserVo> userList = userMapper.searchUserByAuth(auth);
        if (CollectionUtils.isNotEmpty(userList)){
            Iterator<UserVo> iterator = userList.iterator();
            while (iterator.hasNext()){
                UserVo userVo = iterator.next();
                if (roleUserSet.contains(userVo.getUuid())){
                    iterator.remove();
                }
            }
        }
        returnObj.put("roleUserList", roleUserList);
        returnObj.put("userList", userList);
        returnObj.put("userCount", roleUserList.size() + userList.size());
        return returnObj;
    }
}
