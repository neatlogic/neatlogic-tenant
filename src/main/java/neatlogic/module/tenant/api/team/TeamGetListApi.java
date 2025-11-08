/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.api.team;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class TeamGetListApi extends PrivateApiComponentBase {

    @Resource
    private TeamMapper teamMapper;

    @Override
    public String getToken() {
        return "team/get/list";
    }

    @Override
    public String getName() {
        return "批量获取用户组信息";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "teamUuidList", desc = "用户组Uuid集合", type = ApiParamType.JSONARRAY, isRequired = true)
    })
    @Output({
            @Param(name = "teamList", desc = "用户组集合", explode = TeamVo[].class)
    })
    @Description(desc = "批量获取用户组信息")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        returnObj.put("teamList", new ArrayList<>());
        List<String> teamUuidList = JSON.parseArray(jsonObj.getString("teamUuidList"), String.class);
        if (CollectionUtils.isNotEmpty(teamUuidList)) {
            List<TeamVo> teamList = teamMapper.getTeamByUuidList(teamUuidList);
            teamList.sort(Comparator.comparingInt(o -> teamUuidList.indexOf(o.getUuid())));
            returnObj.put("teamList", teamList);
        }
        return returnObj;
    }
}
