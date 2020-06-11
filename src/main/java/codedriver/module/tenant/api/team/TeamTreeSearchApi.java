package codedriver.module.tenant.api.team;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-05 18:49
 **/
@Service
public class TeamTreeSearchApi extends ApiComponentBase {

    @Autowired
    private TeamMapper teamMapper;

    @Override
    public String getToken() {
        return "team/tree/search";
    }

    @Override
    public String getName() {
        return "组织架构树检索接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param( name = "uuid", isRequired = true, desc = "主键ID", xss = true, type = ApiParamType.STRING)
    })
    @Output({
            @Param(
                    name = "children",
                    type = ApiParamType.JSONARRAY,
                    desc = "用户组织架构集合")
    })
    @Description(desc = "组织架构树检索接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String uuid = jsonObj.getString("uuid");
        JSONObject returnObj = new JSONObject();
        JSONArray returnArray = new JSONArray();
	    returnArray.add(iterativeAssembly(uuid, null));
        returnObj.put("children", returnArray);
        return returnObj;
    }
    
    public JSONObject iterativeAssembly(String uuid, JSONObject dataObj){
        TeamVo teamVo = teamMapper.getTeamByUuid(uuid);
        JSONObject teamObj = new JSONObject();
        teamObj.put("name", teamVo.getName());
        teamObj.put("uuid", teamVo.getUuid());
//        teamObj.put("sort", teamVo.getSort());
        teamObj.put("parentUuid", teamVo.getParentUuid());
        teamObj.put("tagList", teamVo.getTagList());
        teamObj.put("userCount", teamVo.getUserCount());
        teamObj.put("childCount", teamVo.getChildCount());
        if (dataObj != null){
            JSONArray childArray = new JSONArray();
            childArray.add(dataObj);
            teamObj.put("children", childArray);
        }
        if (!TeamVo.ROOT_UUID.equals(teamVo.getParentUuid())){
         return iterativeAssembly(teamVo.getParentUuid(), teamObj);
        }
        return teamObj;
    }
}
