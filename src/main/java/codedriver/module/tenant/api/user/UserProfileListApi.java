package codedriver.module.tenant.api.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserProfileVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.userprofile.UserProfileFactory;

@Service
public class UserProfileListApi extends ApiComponentBase {
	@Autowired
	UserMapper userMapper;

	@Override
	public String getToken() {
		return "user/profile/list";
	}

	@Override
	public String getName() {
		return "用户个性化查询接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({})
	@Output({@Param( explode = UserProfileVo.class , desc = "用户个性化查询接口")})
	@Description(desc = "用户个性化查询接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		List<UserProfileVo> myUserProfileList = userMapper.getUserProfileByUserIdAndModuleId(UserContext.get().getUserId(),null);
		Map<String,String> myUserProfileMap = new HashMap<String,String>();
		for(UserProfileVo myUserProfileVo : myUserProfileList) {
			if(StringUtils.isNotBlank(myUserProfileVo.getConfig())){
				JSONArray tmpArray = JSONArray.parseArray(myUserProfileVo.getConfig());
				for(Object tmp : tmpArray) {
					JSONObject tmpJson = (JSONObject)tmp;
					myUserProfileMap.put(tmpJson.getString("value"), tmpJson.getString("text"));
				}
				
			}
		}
		List<UserProfileVo> userProfileList = new ArrayList<UserProfileVo>();
		Map<String, UserProfileVo>  userProfileMap = UserProfileFactory.getUserProfileMap();
		Set<Entry<String, UserProfileVo>> entrySet = userProfileMap.entrySet();
		for(Entry<String, UserProfileVo> entry:entrySet) {
			UserProfileVo usrProfileVo = (UserProfileVo) entry.getValue().clone();
			String config = usrProfileVo.getConfig();
			JSONArray configObjArray = JSONArray.parseArray(config);
			for(Object configObj :configObjArray) {
				JSONObject configJson = (JSONObject)configObj;
				if(myUserProfileMap.containsKey(configJson.getString("value"))) {
					configJson.put("checked", 1);
				}
			}
			usrProfileVo.setConfig(configObjArray.toJSONString());
			userProfileList.add(usrProfileVo);
		}
		return userProfileList; 
	}
}
