/*
 * Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neatlogic.module.tenant.api.region;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.region.RegionMapper;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.auth.label.REGION_MODIFY;
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
            @Param(name = "type", type = ApiParamType.ENUM, rule = "owner,worker", desc = "nmtar.searchregionteamapi.input.param.desc.type", isRequired = true, help = "地域id"),
            @Param(name = "teamUuidList", type = ApiParamType.JSONARRAY, desc = "nmtar.deleteregionteamapi.input.param.desc.teamuuidlist", isRequired = true, help = "分组uuid列表")

    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long id = paramObj.getLong("regionId");
        String type = paramObj.getString("type");
        JSONArray teamUuidArray = paramObj.getJSONArray("teamUuidList");
        regionMapper.deleteRegionTeamByRegionIdAndTypeAndTeamUuidList(id,type,teamUuidArray);
        return null;
    }

    @Override
    public String getToken() {
        return "/region/team/delete";
    }
}
