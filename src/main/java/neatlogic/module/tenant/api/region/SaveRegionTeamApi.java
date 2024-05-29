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
import neatlogic.framework.dto.region.RegionTeamVo;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.auth.label.REGION_MODIFY;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@AuthAction(action = REGION_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
@Transactional
public class SaveRegionTeamApi extends PrivateApiComponentBase {
    @Resource
    RegionMapper regionMapper;

    @Override
    public String getName() {
        return "";
    }


    @Input({
            @Param(name = "regionId", type = ApiParamType.LONG, desc = "nmtar.searchregionteamapi.input.param.desc.regionid", isRequired = true, help = "地域id"),
            @Param(name = "teamList", type = ApiParamType.JSONARRAY, desc = "分组列表", isRequired = true, help = "分组列表"),
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long regionId = paramObj.getLong("regionId");
        JSONArray teamList = paramObj.getJSONArray("teamList");
        Long updateTime = System.currentTimeMillis();
        for (int i = 0; i < teamList.size(); i++) {
            JSONObject team = teamList.getJSONObject(i);
            if (MapUtils.isNotEmpty(team)) {
                String teamUuid = team.getString("uuid");
                Integer checkedChildren = team.getInteger("checkedChildren");
                RegionTeamVo regionTeamVo = new RegionTeamVo(regionId, teamUuid, checkedChildren, updateTime);
                regionMapper.insertRegionTeam(regionTeamVo);
            }
        }
        regionMapper.deleteRegionExpired(regionId, updateTime);
        return null;
    }

    @Override
    public String getToken() {
        return "/region/team/save";
    }
}
