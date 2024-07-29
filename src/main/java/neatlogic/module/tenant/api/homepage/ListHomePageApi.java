package neatlogic.module.tenant.api.homepage;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.HOME_PAGE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.common.constvalue.UserType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.dao.mapper.HomePageMapper;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.*;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@AuthAction(action = HOME_PAGE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ListHomePageApi extends PrivateApiComponentBase {

	@Resource
	private HomePageMapper homePageMapper;

	@Resource
	private UserMapper userMapper;

	@Resource
	private TeamMapper teamMapper;

	@Resource
	private RoleMapper roleMapper;

	@Override
	public String getToken() {
		return "homepage/list";
	}

	@Override
	public String getName() {
		return "首页配置列表";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage")
		})
	@Output({
		@Param(explode = BasePageVo.class),
		@Param(name="tbodyList",explode= HomePageVo[].class,desc="common.tbodylist")
	})
	@Description(desc = "nmpac.channelsearchapi.getname")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		BasePageVo basePageVo = jsonObj.toJavaObject(BasePageVo.class);
		int rowNum = homePageMapper.getHomePageCount(basePageVo);
		if (rowNum > 0) {
			basePageVo.setRowNum(rowNum);
			List<HomePageVo> tbodyList = homePageMapper.getHomePageList(basePageVo);
			for (HomePageVo homePageVo : tbodyList) {
				JSONArray authorityVoArray = new JSONArray();
				List<AuthorityVo> authorityVoList = homePageMapper.getHomePageAuthorityListByHomePageId(homePageVo.getId());
				if (CollectionUtils.isNotEmpty(authorityVoList)) {
					for (AuthorityVo authorityVo : authorityVoList) {
						if (Objects.equals(authorityVo.getType(), GroupSearch.USER.getValue())) {
							UserVo userVo = userMapper.getUserBaseInfoByUuid(authorityVo.getUuid());
							if (userVo != null) {
								authorityVoArray.add(userVo);
							}
						} else if (Objects.equals(authorityVo.getType(), GroupSearch.TEAM.getValue())) {
							TeamVo search = new TeamVo();
							search.setUuid(authorityVo.getUuid());
							TeamVo teamVo = teamMapper.getTeamSimpleInfoByUuid(search);
							if (teamVo != null) {
								authorityVoArray.add(teamVo);
							}
						} else if (Objects.equals(authorityVo.getType(), GroupSearch.ROLE.getValue())) {
							RoleVo roleVo = roleMapper.getRoleSimpleInfoByUuid(authorityVo.getUuid());
							if (roleVo != null) {
								authorityVoArray.add(roleVo);
							}
						} else if (Objects.equals(authorityVo.getType(), GroupSearch.COMMON.getValue())) {
							WorkAssignmentUnitVo workAssignmentUnitVo = new WorkAssignmentUnitVo();
							workAssignmentUnitVo.setUuid(authorityVo.getUuid());
							workAssignmentUnitVo.setName(UserType.getText(authorityVo.getUuid()));
							workAssignmentUnitVo.setInitType(GroupSearch.COMMON.getValue());
							authorityVoArray.add(workAssignmentUnitVo);
						}
					}
				}
				homePageVo.setAuthorityVoList(authorityVoArray);
			}
			return TableResultUtil.getResult(tbodyList, basePageVo);
		}
		return TableResultUtil.getResult(new ArrayList<>(), basePageVo);
	}

}
