/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.tenant.api.user;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
		return "查询用户_下拉";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "keyword",
					type = ApiParamType.STRING,
					desc = "关键字(用户id或名称),模糊查询",
					xss = true),
			@Param(name = "defaultValue",
					type = ApiParamType.JSONARRAY,
					desc = "用于回显的参数列表",
					xss = true),
			@Param(name = "currentPage",
					type = ApiParamType.INTEGER,
					desc = "当前页数",
					isRequired = false),
			@Param(name = "pageSize",
					type = ApiParamType.INTEGER,
					desc = "每页展示数量 默认10",
					isRequired = false),
            @Param(name = "needPage",
                    type = ApiParamType.BOOLEAN,
                    desc = "是否分页")})
	@Output({
			@Param(name = "list",
					type = ApiParamType.JSONARRAY,
					desc = "选项列表"),
			@Param(name = "pageCount",
					type = ApiParamType.INTEGER,
					desc = "总页数"),
			@Param(name = "currentPage",
					type = ApiParamType.INTEGER,
					desc = "当前页数"),
			@Param(name = "pageSize",
					type = ApiParamType.INTEGER,
					desc = "每页展示数量"),
			@Param(name = "rowNum",
					type = ApiParamType.INTEGER,
			 		desc = "总条目数")})
	@Description(desc = "查询用户_下拉")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		
		JSONObject resultObj = new JSONObject();
		resultObj.put("list", new ArrayList<>());
		List<UserVo> userList = null;
		JSONArray defaultValue = jsonObj.getJSONArray("defaultValue");
	    if(CollectionUtils.isNotEmpty(defaultValue)) {
			userList = userMapper.getUserListByUuidList(defaultValue.toJavaList(String.class));
	    }else {
	        UserVo userVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<UserVo>() {});
	        int pageCount = 0;
	        if (userVo.getNeedPage()) {
	            int rowNum = userMapper.searchUserCount(userVo);
	            pageCount = PageUtil.getPageCount(rowNum, userVo.getPageSize());
	            resultObj.put("rowNum", rowNum);
	            resultObj.put("pageSize", userVo.getPageSize());
	            resultObj.put("currentPage", userVo.getCurrentPage());
	            resultObj.put("pageCount", pageCount);
	        }
	        if(!userVo.getNeedPage() || userVo.getCurrentPage() <= pageCount) {
	            userList = userMapper.searchUserForSelect(userVo);
	        }
	    }
	    if(CollectionUtils.isNotEmpty(userList)) {
            JSONArray resultArray = new JSONArray();
	        for(UserVo user : userList) {
                JSONObject userObj = new JSONObject();
                userObj.put("value", user.getUuid());
                userObj.put("text", user.getUserName() + "(" + user.getUserId() + ")");
                userObj.put("name", user.getUserName());
                userObj.put("pinyin", user.getPinyin());
                userObj.put("avatar", user.getAvatar());
                userObj.put("vipLevel", user.getVipLevel());
                resultArray.add(userObj);
            }
            resultObj.put("list", resultArray);
	    }
		return resultObj;
	}
}
