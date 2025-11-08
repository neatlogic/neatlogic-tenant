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

package neatlogic.module.tenant.api.region;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.REGION_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.region.RegionMapper;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.dto.region.RegionTeamVo;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = REGION_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchRegionTeamApi extends PrivateApiComponentBase {
    @Resource
    RegionMapper regionMapper;

    @Override
    public String getName() {
        return "nmtar.searchregionteamapi.getname";
    }


    @Input({
            @Param(name = "regionId", type = ApiParamType.LONG, desc = "nmtar.searchregionteamapi.input.param.desc.regionid", isRequired = true, help = "地域id"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize")
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        RegionTeamVo region = JSON.toJavaObject(paramObj, RegionTeamVo.class);
        List<TeamVo> teamList = new ArrayList<>();
        int rowNum = regionMapper.searchRegionTeamCount(region);
        region.setRowNum(rowNum);
        if (rowNum > 0) {
            teamList = regionMapper.searchRegionTeam(region);
        }

        return TableResultUtil.getResult(teamList, region);
    }

    @Override
    public String getToken() {
        return "/region/team/search";
    }
}
