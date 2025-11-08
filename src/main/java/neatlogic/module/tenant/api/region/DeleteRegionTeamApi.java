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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.REGION_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.region.RegionMapper;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = REGION_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class DeleteRegionTeamApi extends PrivateApiComponentBase {
    @Resource
    RegionMapper regionMapper;

    @Override
    public String getName() {
        return "nmtar.deleteregionteamapi.getname";
    }


    @Input({
            @Param(name = "regionId", type = ApiParamType.LONG, desc = "nmtar.searchregionteamapi.input.param.desc.regionid", isRequired = true, help = "地域id"),
            @Param(name = "teamUuidList", type = ApiParamType.JSONARRAY, desc = "nmtar.deleteregionteamapi.input.param.desc.teamuuidlist", isRequired = true, help = "分组uuid列表")

    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long id = paramObj.getLong("regionId");
        JSONArray teamUuidArray = paramObj.getJSONArray("teamUuidList");
        regionMapper.deleteRegionTeamByRegionIdAndTypeAndTeamUuidList(id, teamUuidArray);
        return null;
    }

    @Override
    public String getToken() {
        return "/region/team/delete";
    }
}
