package codedriver.module.tenant.api.team;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@Transactional
public class RebuidLeftRightCodeApi extends ApiComponentBase {

	@Autowired
	private TeamMapper teamMapper;
	
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
		rebuildLeftRightCode(TeamVo.ROOT_PARENTUUID, 0);
		return null;
	}

	private Integer rebuildLeftRightCode(String parentUuid, Integer parentLft) {
		teamMapper.getTeamLockByUuid(TeamVo.ROOT_UUID);
		List<TeamVo> teamList = teamMapper.getTeamByParentUuid(parentUuid);
		for(TeamVo team : teamList) {
			if(team.getChildCount() == 0) {
				teamMapper.updateTeamLeftRightCode(team.getUuid(), parentLft + 1, parentLft + 2);
				parentLft += 2;
			}else {
				int lft = parentLft + 1;
				parentLft = rebuildLeftRightCode(team.getUuid(), lft);
				teamMapper.updateTeamLeftRightCode(team.getUuid(), lft, parentLft + 1);
				parentLft += 1;
			}
		}
		return parentLft;
	}
}
