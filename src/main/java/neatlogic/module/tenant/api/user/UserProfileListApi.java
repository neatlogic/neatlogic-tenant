/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.user;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.IUserProfile;
import neatlogic.framework.common.constvalue.IUserProfileOperate;
import neatlogic.framework.common.util.ModuleUtil;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserProfileVo;
import neatlogic.framework.dto.module.ModuleVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.userprofile.UserProfileFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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
		return "nmtau.userprofilelistapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param( name="moduleId" , type=ApiParamType.STRING , desc="common.moduleid"),
		@Param(name="name",type=ApiParamType.STRING,desc="common.name")
	})
	@Output({@Param( explode = UserProfileVo.class , desc = "common.tbodylist")})
	@Description(desc = "nmtau.userprofilelistapi.getname")
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
		List<UserProfileVo> userProfileList = new ArrayList<>();
		List<String> activeModuleIdList = new ArrayList<>();
		if (moduleId != null) {
			activeModuleIdList.add(moduleId);
		} else {
			List<ModuleVo> activeModuleList = TenantContext.get().getActiveModuleList();
			for (ModuleVo moduleVo : activeModuleList) {
				activeModuleIdList.add(moduleVo.getId());
			}
		}
		for (String activeModuleId : activeModuleIdList) {
			List<IUserProfile> userProfiles = UserProfileFactory.getUserProfileListByModuleId(activeModuleId);
			if (CollectionUtils.isEmpty(userProfiles)) {
				continue;
			}
			UserProfileVo userProfileVo = new UserProfileVo();
			userProfileVo.setModuleId(activeModuleId);
			userProfileVo.setModuleName(ModuleUtil.getModuleById(activeModuleId).getName());
			JSONArray userProfileArray = new JSONArray();
			for (IUserProfile userProfile : userProfiles) {
				if (StringUtils.isNotBlank(name) && !Objects.equals(name, userProfile.getValue())) {
					continue;
				}
				List<IUserProfileOperate> profileOperateList = userProfile.getProfileOperateList();
				if (CollectionUtils.isEmpty(profileOperateList)) {
					continue;
				}
				JSONObject userProfileObj = new JSONObject();
				userProfileObj.put("value", userProfile.getValue());
				userProfileObj.put("text", userProfile.getText());
				userProfileObj.put("checked", 1);
				JSONArray userProfileOperateArray = new JSONArray();
				for (IUserProfileOperate userProfileOperate : profileOperateList) {
					JSONObject userProfileOperateObj = new JSONObject();
					userProfileOperateObj.put("value", userProfileOperate.getValue());
					userProfileOperateObj.put("text", userProfileOperate.getText());
					String key = activeModuleId + userProfile.getValue() + userProfileOperate.getValue();
					if (myUserProfileOperateMap.containsKey(key)) {
						userProfileOperateObj.put("checked", 1);
						userProfileObj.put("checked", 0);
					} else {
						userProfileOperateObj.put("checked", 0);
					}
					userProfileOperateArray.add(userProfileOperateObj);
				}
				userProfileObj.put("userProfileOperateList", userProfileOperateArray);
				userProfileArray.add(userProfileObj);
			}
			userProfileVo.setConfig(userProfileArray.toJSONString());
			userProfileList.add(userProfileVo);
		}
		return userProfileList;
	}
}
