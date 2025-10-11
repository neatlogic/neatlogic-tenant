package neatlogic.module.tenant.api.team;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.TEAM_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.TeamLevel;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@AuthAction(action = TEAM_MODIFY.class)

@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
public class TeamUpdateApi extends PrivateApiComponentBase {

    @Resource
    private TeamMapper teamMapper;


    @Override
    public String getToken() {
        return "team/update";
    }

    @Override
    public String getName() {
        return "更新分组信息";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "name", type = ApiParamType.STRING, desc = "common.name", isRequired = true),
            @Param(name = "email", type = ApiParamType.STRING, desc = "common.email", maxLength = 100),
            @Param(name = "phone", type = ApiParamType.STRING, desc = "common.phone", maxLength = 20),
            @Param(name = "level", type = ApiParamType.ENUM, member = TeamLevel.class, desc = "common.level"),
    })
    @Output({@Param(name = "uuid", type = ApiParamType.STRING, desc = "common.uuid")})
    @Description(desc = "更新分组信息，仅更新提供值的属性，不提供值的属性不更新。如果成功更新，则返回被更新分组uuid，如果没更新，则返回空值")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String level = jsonObj.getString("level");
        String email = jsonObj.getString("email");
        String phone = jsonObj.getString("phone");
        String name = jsonObj.getString("name");
        List<TeamVo> teamList = teamMapper.getTeamByNameList(Collections.singletonList(name));
        if (CollectionUtils.isNotEmpty(teamList) && teamList.size() == 1) {
            TeamVo teamVo = new TeamVo();
            teamVo.setUuid(teamList.get(0).getUuid());
            teamVo.setName(teamList.get(0).getName());
            teamVo.setLevel(level);
            teamVo.setEmail(email);
            teamVo.setPhone(phone);
            teamMapper.updateTeamOptionalByUuid(teamVo);
            return teamVo.getUuid();
        }
        return null;
    }


}
