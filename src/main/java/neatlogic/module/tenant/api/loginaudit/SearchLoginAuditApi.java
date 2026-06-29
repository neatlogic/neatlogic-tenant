package neatlogic.module.tenant.api.loginaudit;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.auth.label.USER_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.dao.mapper.LoginMapper;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.dto.loginaudit.LoginAuditVo;
import neatlogic.framework.dto.loginaudit.LoginAuditSearchVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.framework.util.TimeUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Component
@AuthAction(action = USER_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchLoginAuditApi extends PrivateApiComponentBase {

    @Resource
    private LoginMapper loginMapper;
    @Resource
    private TeamMapper teamMapper;

    @Override
    public String getName() {
        return "nmtal.searchloginauditapi.getname";
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize"),
            @Param(name = "timeRange", type = ApiParamType.INTEGER, desc = "common.timerange"),
            @Param(name = "timeUnit", type = ApiParamType.STRING, desc = "common.timeunit"),
            @Param(name = "startTime", type = ApiParamType.LONG, desc = "common.starttime"),
            @Param(name = "endTime", type = ApiParamType.LONG, desc = "common.endtime"),
            @Param(name = "teamUuidList", type = ApiParamType.JSONARRAY, desc = "common.teamuuidlist"),
            @Param(name = "moduleGroupList", type = ApiParamType.JSONARRAY, desc = "common.modulegroup"),
            @Param(name = "featureNameList", type = ApiParamType.JSONARRAY, desc = "common.featurename"),
    })
    @Output({
            @Param(name = "tbodylist", explode = LoginAuditVo[].class, desc = "common.tbodylist"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "nmtal.searchloginauditapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        LoginAuditSearchVo searchVo = paramObj.toJavaObject(LoginAuditSearchVo.class);
        if (CollectionUtils.isNotEmpty(searchVo.getTeamUuidList())) {
            searchVo.setTeamUuidList(searchVo.getTeamUuidList().stream().map(GroupSearch::removePrefix).collect(Collectors.toList()));
        }
        //将时间范围转为 开始时间、结束时间
        if (searchVo.getStartTime() == null && searchVo.getEndTime() == null) {
            Integer timeRange = paramObj.getInteger("timeRange");
            String timeUnit = paramObj.getString("timeUnit");
            if (timeRange != null && StringUtils.isNotBlank(timeUnit)) {
                searchVo.setStartTime(TimeUtil.recentTimeTransfer(timeRange, timeUnit));
                searchVo.setEndTime(new Date());
            }
        }
        List<LoginAuditVo> tbodyList = new ArrayList<>();
        int rowNum = loginMapper.getLoginAuditCount(searchVo);
        if (rowNum > 0) {
            searchVo.setRowNum(rowNum);
            Map<String, List<String>> userUuid2teamNameListMap = new HashMap<>();
            tbodyList = loginMapper.getLoginAuditList(searchVo);
            for (LoginAuditVo loginAuditVo : tbodyList) {
                if (StringUtils.isNotBlank(loginAuditVo.getUserUuid())) {
                    List<String> teamNameList = userUuid2teamNameListMap.get(loginAuditVo.getUserUuid());
                    if (teamNameList == null) {
                        teamNameList = new ArrayList<>();
                        List<TeamVo> teamList = teamMapper.getTeamListByUserUuid(loginAuditVo.getUserUuid());
                        if (CollectionUtils.isNotEmpty(teamList)) {
                            teamNameList = teamList.stream().map(TeamVo::getName).collect(Collectors.toList());
                        }
                        userUuid2teamNameListMap.put(loginAuditVo.getUserUuid(), teamNameList);
                    }
                    loginAuditVo.setTeamNameList(new ArrayList<>(teamNameList));
                }
            }
        }
        return TableResultUtil.getResult(tbodyList, searchVo);
    }

    @Override
    public String getToken() {
        return "login/audit/list";
    }
}
