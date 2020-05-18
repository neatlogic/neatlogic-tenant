package codedriver.module.tenant.api.user;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserActiveUpdateApi extends ApiComponentBase {

    @Autowired
    private UserMapper userMapper;

    @Override
    public String getToken() {
        return "user/active";
    }

    @Override
    public String getName() {
        return "用户有效性变更接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "userUuidList", type = ApiParamType.JSONARRAY, desc = "用户uuid集合",isRequired = true),
            @Param(name = "isActive", type = ApiParamType.STRING, desc = "有效性", isRequired = true)
    })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
//        JSONArray userIdList = jsonObj.getJSONArray("userIdList");
//        for (int i = 0; i < userIdList.size(); i++){
//            UserVo userVo = new UserVo();
//            userVo.setUserId(userIdList.getString(i));
//            userVo.setIsActive(jsonObj.getInteger("isActive"));
//            userService.updateUserActive(userVo);
//        }
        Integer isActive = jsonObj.getInteger("isActive");
    	List<String> userUuidList = JSON.parseArray(jsonObj.getString("userUuidList"), String.class);
    	if(CollectionUtils.isNotEmpty(userUuidList)) {
			UserVo userVo = new UserVo();
            userVo.setIsActive(isActive);
    		for(String userUuid : userUuidList) {
                userVo.setUuid(userUuid);
                userMapper.updateUserActive(userVo);
    		}
    	}
        return null;
    }
}
