package codedriver.module.tenant.api.user;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserProfileVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.userprofile.UserProfileFactory;
import codedriver.module.tenant.exception.user.UserProfileModuleNotFoundException;

@Service
public class UserProfileSaveApi extends ApiComponentBase {
	@Autowired
	UserMapper userMapper;

	@Override
	public String getToken() {
		return "user/profile/save";
	}

	@Override
	public String getName() {
		return "用户个性化保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name="moduleId",type=ApiParamType.STRING,isRequired=true,desc="模块id"),
		@Param(name="checked",type=ApiParamType.INTEGER,isRequired=true,desc="是否勾选，1：勾选，0：不勾选"),
		@Param(name="name",type=ApiParamType.STRING,isRequired=true,desc="操作个性化选项名")
	})
	@Output({})
	@Description(desc = "用户个性化保存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String moduleId = jsonObj.getString("moduleId");
		String name = jsonObj.getString("name");
		String userId =UserContext.get().getUserId();
		Integer checked = jsonObj.getInteger("checked");
		if(!UserProfileFactory.getUserProfileMap().containsKey(moduleId)) {
			throw new UserProfileModuleNotFoundException(moduleId);
		}
		//找出对用的json
		String config = UserProfileFactory.getUserProfileMap().get(moduleId).getConfig();
		JSONArray configArray = JSONObject.parseArray(config);
		List<Object> list = configArray.stream().filter(o->((JSONObject)o).containsValue(name)).collect(Collectors.toList());
		if(CollectionUtils.isNotEmpty(list)) {
			List<UserProfileVo> myUserProfileList = userMapper.getUserProfileByUserIdAndModuleId(userId,moduleId);
			if(CollectionUtils.isNotEmpty(myUserProfileList)) {//存在用户记录 update
				UserProfileVo userProfileVo = myUserProfileList.get(0);
				String myConfig = userProfileVo.getConfig();
				if(StringUtils.isNotBlank(myConfig)) {
					JSONArray myConfigArray = JSONArray.parseArray(myConfig);
					List<Object> myList = myConfigArray.stream().filter(o->((JSONObject)o).containsValue(name)).collect(Collectors.toList());
					if(CollectionUtils.isEmpty(myList)) {
						if(checked == 1) {
							myConfigArray.add(list.get(0));
						}else {
							//do nothing
						}
					}else {
						if(checked == 0) {
							myConfigArray.remove(list.get(0));
						}else {
							//do nothing
						}
					}
					if(CollectionUtils.isNotEmpty(myConfigArray)) {
						userMapper.updateUserProfileByUserIdAndModuleId(userId, moduleId, myConfigArray.toJSONString());
					}else {//config 为空，则删除用户记录
						userMapper.deleteUserProfileByUserIdAndModuleId(userId, moduleId);
					}
				}else {
					//do nothing
				}
			}else {//不存在用户记录 insert
				if(checked == 1) {
					UserProfileVo userProfileVo = new UserProfileVo();
					JSONArray myConfigArray = new JSONArray();
					myConfigArray.add(list.get(0));
					userProfileVo.setModuleId(moduleId);
					userProfileVo.setUserId(userId);
					userProfileVo.setConfig(myConfigArray.toJSONString());
					userMapper.insertUserProfile(userProfileVo);
				}else {
					// do nothing
				}
			}
		}
		return null;
	}
}
