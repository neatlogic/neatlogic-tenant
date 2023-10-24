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
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class UserSearchForSelectApi extends PrivateApiComponentBase {

	@Autowired
	private UserMapper userMapper;

	@Override
	public String getToken() {
		return "user/search/forselect";
	}

	@Override
	public String getName() {
		return "nmtau.usersearchforselectapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword", xss = true),
			@Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", desc = "common.isactive"),
			@Param(name = "needTeam", type = ApiParamType.BOOLEAN, desc = "nmtau.usersearchforselectapi.input.param.desc.needteam"),
			@Param(name = "teamUuid", type = ApiParamType.STRING, desc = "common.teamuuid"),
			@Param(name = "roleUuid", type = ApiParamType.STRING, desc = "common.roleuuid"),
			@Param(name = "defaultValue", type = ApiParamType.JSONARRAY, desc = "common.defaultvalue", xss = true),
			@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
			@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize")
	})
	@Output({
			@Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "common.tbodylist"),
			@Param(explode = BasePageVo.class)
	})
	@Description(desc = "nmtau.usersearchforselectapi.getname")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		UserVo searchVo = jsonObj.toJavaObject(UserVo.class);
		List<UserVo> tbodyList = new ArrayList<>();
		List<UserVo> userList = new ArrayList<>();
		JSONArray defaultValue = searchVo.getDefaultValue();
		if (CollectionUtils.isNotEmpty(defaultValue)) {
			List<String> uuidList = defaultValue.toJavaList(String.class);
			userList = userMapper.getUserListByUuidList(uuidList);
		} else {
			int rowNum = userMapper.searchUserCount(searchVo);
			if (rowNum > 0) {
				searchVo.setRowNum(rowNum);
				List<String> userUuidList = userMapper.searchUserBaseInfo(searchVo);
				if (CollectionUtils.isNotEmpty(userUuidList)) {
					userList = userMapper.searchUserDetailInfoByUuidList(userUuidList);
				}
			}
		}
		if (CollectionUtils.isNotEmpty(userList)) {
			Boolean needTeam = jsonObj.getBoolean("needTeam");
			for (UserVo user : userList) {
				UserVo userVo = new UserVo(user.getUuid(), user.getUserId(), user.getUserName());
				if (Objects.equals(needTeam, true)) {
					userVo.setTeamNameList(user.getTeamNameList());
				}
				tbodyList.add(userVo);
			}
		}
		return TableResultUtil.getResult(tbodyList, searchVo);
	}
}
