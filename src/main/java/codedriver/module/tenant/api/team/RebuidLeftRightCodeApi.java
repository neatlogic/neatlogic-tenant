package codedriver.module.tenant.api.team;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.module.tenant.service.TeamService;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
public class RebuidLeftRightCodeApi extends PrivateApiComponentBase {

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
		teamMapper.getTeamCountOnLock();
		teamService.rebuildLeftRightCode();
		return null;
	}

}
