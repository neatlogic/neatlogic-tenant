package codedriver.module.tenant.api.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
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
		Map<String,String> myUserProfileOperateMap = new HashMap<String,String>();
		for(UserProfileVo myUserProfileVo : myUserProfileList) {
			if(StringUtils.isNotBlank(myUserProfileVo.getConfig())){
				JSONArray tmpArray = JSONArray.parseArray(myUserProfileVo.getConfig());
				for(Object tmp : tmpArray) {
					JSONObject tmpJson = (JSONObject)tmp;
					JSONArray operateArray = tmpJson.getJSONArray("userProfileOperateList");
					if(CollectionUtils.isNotEmpty(operateArray)) {
						for(Object operateObj:operateArray) {
							JSONObject operateJson = (JSONObject)operateObj;
							myUserProfileOperateMap.put(operateJson.getString("value"),operateJson.getString("text"));
						}
					}
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
				configJson.put("checked", 1);
				JSONArray operateArray = configJson.getJSONArray("userProfileOperateList");
				if(CollectionUtils.isNotEmpty(operateArray)) {
					for(Object operateObj:operateArray) {
						JSONObject operateJson = (JSONObject)operateObj;
						if(myUserProfileOperateMap.containsKey(operateJson.getString("value"))) {
							operateJson.put("checked", 1);
							configJson.put("checked", 0);
						}else {
							operateJson.put("checked", 0);
						}
					}
				}
				
			}
			usrProfileVo.setConfig(configObjArray.toJSONString());
			userProfileList.add(usrProfileVo);
		}
		return userProfileList; 
	}
}
