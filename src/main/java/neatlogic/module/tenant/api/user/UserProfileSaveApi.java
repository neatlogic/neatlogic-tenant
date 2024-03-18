/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tenant.api.user;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.IUserProfile;
import neatlogic.framework.common.constvalue.IUserProfileOperate;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserProfileVo;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.userprofile.UserProfileFactory;
import neatlogic.module.tenant.exception.user.UserProfileModuleNotFoundException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional

@OperationType(type = OperationTypeEnum.UPDATE)
public class UserProfileSaveApi extends PrivateApiComponentBase {
	@Autowired
	UserMapper userMapper;

	@Override
	public String getToken() {
		return "user/profile/save";
	}

	@Override
	public String getName() {
		return "nmtau.userprofilesaveapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name="moduleId",type=ApiParamType.STRING,isRequired=true,desc="common.moduleid"),
		@Param(name="checked",type=ApiParamType.INTEGER,isRequired=true,desc="nmtau.userprofilesaveapi.input.param.checked.desc", help = "1：勾选，0：不勾选"),
		@Param(name="name",type=ApiParamType.STRING,isRequired=true,desc="nmtau.userprofilesaveapi.input.param.name.desc"),
		@Param(name="operate",type=ApiParamType.STRING,desc="nmtau.userprofilesaveapi.input.param.operate.desc", help = "checked = 0时，必填。")
	})
	@Output({})
	@Description(desc = "nmtau.userprofilesaveapi.getname")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String moduleId = jsonObj.getString("moduleId");
		String name = jsonObj.getString("name");
		String operate = jsonObj.getString("operate");
		String userUuid =UserContext.get().getUserUuid(true);
		Integer checked = jsonObj.getInteger("checked");
		List<IUserProfile> userProfileList = UserProfileFactory.getUserProfileListByModuleId(moduleId);
		if(CollectionUtils.isEmpty(userProfileList)) {
			throw new UserProfileModuleNotFoundException(moduleId);
		}
		if(checked == 0&&StringUtils.isBlank(operate)) {
			throw new ParamIrregularException("operate");
		}
		//找出对用的json
		JSONObject userProfileObj = null;
		JSONObject userProfileOperateObj = null;
		for (IUserProfile userProfile : userProfileList) {
			if (!Objects.equals(name, userProfile.getValue())) {
				continue;
			}
			userProfileObj = new JSONObject();
			userProfileObj.put("value", userProfile.getValue());
			userProfileObj.put("text", userProfile.getText());
			if (checked == 1) {
				userProfileObj.put("checked", 1);
			} else {
				userProfileObj.put("checked", 0);
				List<IUserProfileOperate> profileOperateList = userProfile.getProfileOperateList();
				if (CollectionUtils.isEmpty(profileOperateList)) {
					continue;
				}
				for (IUserProfileOperate userProfileOperate : profileOperateList) {
					if (!Objects.equals(operate, userProfileOperate.getValue())) {
						continue;
					}
					userProfileOperateObj = new JSONObject();
					userProfileOperateObj.put("value", userProfileOperate.getValue());
					userProfileOperateObj.put("text", userProfileOperate.getText());
					userProfileOperateObj.put("checked", 1);
				}
			}
		}
		if (userProfileObj == null) {
			// 参数name无效
			throw new UserProfileModuleNotFoundException(name);
		}
		if (checked == 0) {
			if (userProfileOperateObj == null) {
				// 参数operate无效
				throw new UserProfileModuleNotFoundException(operate);
			}
			JSONArray userProfileOperateArray = new JSONArray();
			userProfileOperateArray.add(userProfileOperateObj);
			userProfileObj.put("userProfileOperateList", userProfileOperateArray);
		}

		List<UserProfileVo> myUserProfileList = userMapper.getUserProfileByUserUuidAndModuleId(userUuid, moduleId);
		if(CollectionUtils.isNotEmpty(myUserProfileList)) {//存在用户记录 update
			UserProfileVo userProfileVo = myUserProfileList.get(0);
			String myConfig = userProfileVo.getConfig();
			if(StringUtils.isNotBlank(myConfig)) {
				JSONArray newUserProfileArray = new JSONArray();
				JSONArray oldUserProfileArray = JSONArray.parseArray(myConfig);
				for (int i = 0; i < oldUserProfileArray.size(); i++) {
					JSONObject oldUserProfileObj = oldUserProfileArray.getJSONObject(i);
					if(!Objects.equals(oldUserProfileObj.getString("value"), name)) {
						newUserProfileArray.add(oldUserProfileObj);
					}
				}
				if (checked == 0) {
					newUserProfileArray.add(userProfileObj);
				}
				if(CollectionUtils.isNotEmpty(newUserProfileArray)) {
					userMapper.updateUserProfileByUserUuidAndModuleId(userUuid, moduleId, newUserProfileArray.toJSONString());
				}else {//config 为空，则删除用户记录
					userMapper.deleteUserProfileByUserUuidAndModuleId(userUuid, moduleId);
				}
			}
		}else {//不存在用户记录 insert
			if(checked == 0) {
				UserProfileVo userProfileVo = new UserProfileVo();
				JSONArray userProfileArray = new JSONArray();
				userProfileArray.add(userProfileObj);
				userProfileVo.setModuleId(moduleId);
				userProfileVo.setUserUuid(userUuid);
				userProfileVo.setConfig(userProfileArray.toJSONString());
				userMapper.insertUserProfile(userProfileVo);
			}
		}
		return null;
	}
}
