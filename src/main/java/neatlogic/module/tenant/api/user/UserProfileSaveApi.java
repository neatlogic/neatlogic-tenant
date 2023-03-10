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

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserProfileVo;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.userprofile.UserProfileFactory;
import neatlogic.module.tenant.exception.user.UserProfileModuleNotFoundException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
		return "???????????????????????????";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name="moduleId",type=ApiParamType.STRING,isRequired=true,desc="??????id"),
		@Param(name="checked",type=ApiParamType.INTEGER,isRequired=true,desc="??????????????????????????????1????????????0????????????"),
		@Param(name="name",type=ApiParamType.STRING,isRequired=true,desc="????????????????????????"),
		@Param(name="operate",type=ApiParamType.STRING,desc="?????????????????????????????????checked = 0???????????????")
	})
	@Output({})
	@Description(desc = "???????????????????????????")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String moduleId = jsonObj.getString("moduleId");
		String name = jsonObj.getString("name");
		String operate = jsonObj.getString("operate");
		String userUuid =UserContext.get().getUserUuid(true);
		Integer checked = jsonObj.getInteger("checked");
		if(!UserProfileFactory.getUserProfileMap().containsKey(moduleId)) {
			throw new UserProfileModuleNotFoundException(moduleId);
		}
		if(checked == 0&&StringUtils.isBlank(operate)) {
			throw new ParamIrregularException("operate");
		}
		//???????????????json
		String config = UserProfileFactory.getUserProfileMap().get(moduleId).getConfig();
		JSONArray configArray = JSONObject.parseArray(config);
		List<Object> list = configArray.stream().filter(o->((JSONObject)o).containsValue(name)).collect(Collectors.toList());
		if(CollectionUtils.isNotEmpty(list)) {
			JSONObject profileJson = (JSONObject)list.get(0);
			JSONArray operateList = profileJson.getJSONArray("userProfileOperateList");
			List<Object> operateTmpList =  operateList.stream().filter(o->((JSONObject)o).containsValue(operate)).collect(Collectors.toList());
			profileJson.put("userProfileOperateList", operateTmpList);
			List<UserProfileVo> myUserProfileList = userMapper.getUserProfileByUserUuidAndModuleId(userUuid, moduleId);
			if(CollectionUtils.isNotEmpty(myUserProfileList)) {//?????????????????? update
				UserProfileVo userProfileVo = myUserProfileList.get(0);
				String myConfig = userProfileVo.getConfig();
				if(StringUtils.isNotBlank(myConfig)) {
					JSONArray myConfigArray = JSONArray.parseArray(myConfig);
					java.util.ListIterator<Object> myConfigIterator =  myConfigArray.listIterator();
					Boolean isExist = false;
					while(myConfigIterator.hasNext()) {
						JSONObject myConfigJson = (JSONObject)myConfigIterator.next();
						if(myConfigJson.getString("value").equals(name)) {
							isExist = true;
							if(checked == 0) {
								myConfigJson.put("userProfileOperateList", operateTmpList);
							}else {
								myConfigIterator.remove();
							}
						}
					}
					if(!isExist) {
						if(checked == 1) {
							//do nothing
						}else {
							myConfigArray.add(list.get(0));
						}
					}
					if(CollectionUtils.isNotEmpty(myConfigArray)) {
						userMapper.updateUserProfileByUserUuidAndModuleId(userUuid, moduleId, myConfigArray.toJSONString());
					}else {//config ??????????????????????????????
						userMapper.deleteUserProfileByUserUuidAndModuleId(userUuid, moduleId);
					}
				}else {
					//do nothing
				}
			}else {//????????????????????? insert
				if(checked == 0) {
					UserProfileVo userProfileVo = new UserProfileVo();
					JSONArray myConfigArray = new JSONArray();
					myConfigArray.add(list.get(0));
					userProfileVo.setModuleId(moduleId);
					userProfileVo.setUserUuid(userUuid);
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
