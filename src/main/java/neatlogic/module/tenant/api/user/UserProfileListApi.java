/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.tenant.api.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserProfileVo;
import neatlogic.framework.userprofile.UserProfileFactory;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class UserProfileListApi extends PrivateApiComponentBase {
	@Autowired
	UserMapper userMapper;

	@Override
	public String getToken() {
		return "user/profile/list";
	}

	@Override
	public String getName() {
		return "用户个性化查询列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param( name="moduleId" , type=ApiParamType.STRING , desc="模块Id"),
		@Param(name="name",type=ApiParamType.STRING,desc="操作个性化选项名")
	})
	@Output({@Param( explode = UserProfileVo.class , desc = "用户个性化查询接口")})
	@Description(desc = "用户个性化查询列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String moduleId = jsonObj.getString("moduleId");
		String name = jsonObj.getString("name");
		List<UserProfileVo> myUserProfileList = userMapper.getUserProfileByUserUuidAndModuleId(UserContext.get().getUserUuid(), moduleId);
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
							myUserProfileOperateMap.put(myUserProfileVo.getModuleId()+tmpJson.getString("value")+operateJson.getString("value"),operateJson.getString("text"));
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
			if(StringUtils.isNotBlank(moduleId)&&!moduleId.equals(usrProfileVo.getModuleId())) {
				continue;
			}
			String config = usrProfileVo.getConfig();
			JSONArray configObjArray = JSONArray.parseArray(config);
			ListIterator<Object> configIterator = configObjArray.listIterator();
			while(configIterator.hasNext()) {
				JSONObject configJson = (JSONObject)configIterator.next();
				if(StringUtils.isNotBlank(name)&&!name.equals(configJson.getString("value"))) {
					configIterator.remove();
					continue;
				}
				configJson.put("checked", 1);
				JSONArray operateArray = configJson.getJSONArray("userProfileOperateList");
				if(CollectionUtils.isNotEmpty(operateArray)) {
					for(Object operateObj:operateArray) {
						JSONObject operateJson = (JSONObject)operateObj;
						if(myUserProfileOperateMap.containsKey(usrProfileVo.getModuleId()+configJson.getString("value")+operateJson.getString("value"))) {
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
