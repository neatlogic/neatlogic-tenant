package codedriver.module.tenant.api.team;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.TeamService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-07 17:39
 **/
@Service
public class TeamUserSaveApi extends ApiComponentBase {

    @Autowired
    private TeamService teamService;

    @Override
    public String getToken() {
        return "team/user/save";
    }

    @Override
    public String getName() {
        return "分组用户保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param( name = "teamUuid", isRequired = true, desc = "分组uuid", type = ApiParamType.STRING),
            @Param( name = "userUuidList", desc = "用户Uuid集合", type = ApiParamType.JSONARRAY)
    })
    @Description( desc = "分组用户保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<String> userUuidList = new ArrayList<>();
        if (jsonObj.containsKey("userUuidList")){
            JSONArray userUuidArray = jsonObj.getJSONArray("userUuidList");
            for (int i = 0 ; i < userUuidArray.size(); i++){
            	userUuidList.add(userUuidArray.getString(i));
            }
        }
        teamService.saveTeamUser(userUuidList, jsonObj.getString("teamUuid"));
        return null;
    }
}
