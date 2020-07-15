package codedriver.module.tenant.api.team;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.TeamService;

@Service
@Transactional
public class RebuidLeftRightCodeApi extends ApiComponentBase {

	@Autowired
	private TeamMapper teamMapper;
	
	@Autowired
	private TeamService teamService;
	
	@Override
	public String getToken() {
		return "team/rebuidleftrightcode";
	}

	@Override
	public String getName() {
		return "用户组重建左右编码接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		teamMapper.getTeamLockByUuid(TeamVo.ROOT_UUID);
		teamService.rebuildLeftRightCode(TeamVo.ROOT_PARENTUUID, 0);
		return null;
	}

}
