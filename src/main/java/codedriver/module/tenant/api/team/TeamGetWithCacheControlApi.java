package codedriver.module.tenant.api.team;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.FRAMEWORK_BASE;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.CacheControlType;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.exception.team.TeamNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = FRAMEWORK_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class TeamGetWithCacheControlApi extends PrivateApiComponentBase {

    @Autowired
    private TeamMapper teamMapper;

    @Override
    public String getToken() {
        return "team/cache/get";
    }

    @Override
    public String getName() {
        return "获取组信息接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid",
                    type = ApiParamType.STRING,
                    desc = "分组uuid",
                    minLength = 32,
                    maxLength = 32,
                    isRequired = true)
    })
    @Output({
            @Param(name = "teamVo",
                    explode = TeamVo.class,
                    desc = "组id")})
    @Description(desc = "获取组信息接口，前端会缓存30000秒，后端mybatis二级缓存300秒")
    @CacheControl(cacheControlType = CacheControlType.MAXAGE, maxAge = 30000)
    @Override
    public Object myDoService(JSONObject jsonObj) {
        String uuid = jsonObj.getString("uuid");
        TeamVo team = new TeamVo();
        team.setUuid(uuid);
        TeamVo teamVo = teamMapper.getTeamSimpleInfoByUuid(team);
        if (teamVo == null) {
            throw new TeamNotFoundException(uuid);
        }
        return teamVo;
    }

}
