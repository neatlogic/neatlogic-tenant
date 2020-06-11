package codedriver.module.tenant.api.user;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.user.UserNotFoundException;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-06 11:49
 **/
@Service
public class UserGetListApi extends ApiComponentBase {

    @Autowired
    private UserMapper userMapper;

    @Override
    public String getToken() {
        return "user/get/list";
    }

    @Override
    public String getName() {
        return "批量获取用户信息接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param( name = "userUuidList", type = ApiParamType.JSONARRAY, desc = "用户uuid集合", isRequired = true)
    })
    @Output({
            @Param( name = "userList", explode = UserVo[].class, desc = "用户信息集合")
    })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        List<String> userUuidList = JSON.parseArray(jsonObj.getString("userUuidList"), String.class);
        List<UserVo> userList = new ArrayList<>();
        for (String userUuid : userUuidList){
            UserVo userVo = userMapper.getUserByUuid(userUuid);
            if(userVo == null) {
    			throw new UserNotFoundException(userUuid);
    		}else {
                userList.add(userVo);
            }
        }
        returnObj.put("userList", userList);
        return returnObj;
    }
}
