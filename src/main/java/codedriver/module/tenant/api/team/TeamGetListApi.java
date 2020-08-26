package codedriver.module.tenant.api.team;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.TeamVo;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-20 10:43
 **/
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class TeamGetListApi extends PrivateApiComponentBase {

    @Autowired
    private TeamMapper teamMapper;

    @Override
    public String getToken() {
        return "team/get/list";
    }

    @Override
    public String getName() {
        return "批量获取用户组信息接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param( name = "teamUuidList", desc = "用户组Uuid集合", type = ApiParamType.JSONARRAY, isRequired = true)
    })
    @Output({
            @Param( name = "teamList", desc = "用户组集合", explode = TeamVo[].class)
    })
    @Description(desc = "批量获取用户组信息接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        returnObj.put("teamList", new ArrayList<>());
        List<String> teamUuidList = JSON.parseArray(jsonObj.getString("teamUuidList"), String.class);
        if(CollectionUtils.isNotEmpty(teamUuidList)) {
        	List<TeamVo> teamList = teamMapper.getTeamByUuidList(teamUuidList);
        	teamList.sort(new Comparator<TeamVo>() {
				@Override
				public int compare(TeamVo o1, TeamVo o2) {
					return teamUuidList.indexOf(o1.getUuid()) - teamUuidList.indexOf(o2.getUuid());
				}       		
        	});
            returnObj.put("teamList", teamList);
        }
        return returnObj;
    }
}
