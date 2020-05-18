package codedriver.module.tenant.api.user;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.AuthVo;
import codedriver.framework.dto.UserAuthVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
public class UserAuthSaveApi extends ApiComponentBase {

    @Autowired
    private UserMapper userMapper;

    @Override
    public String getToken() {
        return "user/auth/save";
    }

    @Override
    public String getName() {
        return "用户权限保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "userUuidList",
            type = ApiParamType.JSONARRAY,
            desc = "用户uuid集合",
            isRequired = true),
            @Param(name = "userAuthList",
            type = ApiParamType.JSONOBJECT,
            desc = "用户权限对象",
            isRequired = true),
            @Param(name = "action",
            type = ApiParamType.STRING,
            desc = "保存类型",
            isRequired = true)
    })
    @Description( desc = "用户权限保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray userUuidArray = jsonObj.getJSONArray("userUuidList");
        String action = jsonObj.getString("action");
        for (int i = 0; i < userUuidArray.size(); i++){
            UserVo userVo = new UserVo();
            userVo.setUuid(userUuidArray.getString(i));
            JSONObject userAuthObj = jsonObj.getJSONObject("userAuthList");
            List<UserAuthVo> userAuthVoList = new ArrayList<>();
            Set<String> keySet = userAuthObj.keySet();
            for (String key : keySet){
                JSONArray authArray = userAuthObj.getJSONArray(key);
                for (int j = 0; j < authArray.size(); j++){
                    UserAuthVo authVo = new UserAuthVo();
                    authVo.setAuth(authArray.getString(j));
                    authVo.setAuthGroup(key);
                    authVo.setUserUuid(userVo.getUuid());
                    userAuthVoList.add(authVo);
                }
            }
            userVo.setUserAuthList(userAuthVoList);
            if (AuthVo.AUTH_ADD.equals(action)){
                List<UserAuthVo> userAuthList = userMapper.searchUserAuthByUserUuid(userVo.getUuid());
        		Set<String> set = new HashSet<>();
        		for (UserAuthVo authVo : userAuthList){
        			set.add(authVo.getAuth());
        		}
        		for (UserAuthVo authVo : userVo.getUserAuthList()){
        			if (!set.contains(authVo.getAuth())){
        				userMapper.insertUserAuth(authVo);
        			}
        		}
            }
            if (AuthVo.AUTH_COVER.equals(action)){
                userMapper.deleteUserAuthByUserUuid(userVo.getUuid());
        		if (userVo.getUserAuthList() != null && userVo.getUserAuthList().size() > 0){
        			for (UserAuthVo authVo : userVo.getUserAuthList()){
        				userMapper.insertUserAuth(authVo);
        			}
        		}
            }
            if (AuthVo.AUTH_DELETE.equals(action)){
            	userMapper.deleteUserAuth(userVo);
            }
        }
        return null;
    }
}
