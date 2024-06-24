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

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.region.RegionMapper;
import neatlogic.framework.dto.region.RegionVo;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchRegionTreeApi extends PrivateApiComponentBase {
    @Resource
    RegionMapper regionMapper;

    @Override
    public String getName() {
        return "nmtar.searchregiontreeapi.getname";
    }

    @Input({
            @Param(name = "isActive", type = ApiParamType.INTEGER, desc = "common.isactive", help = "是否激活")
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Integer isActive = null;
        if (paramObj.containsKey("isActive")) {
            isActive = paramObj.getInteger("isActive");
        }
        Map<Long, RegionVo> idKeyMap = new HashMap<>();
        Integer maxRhtCode = regionMapper.getMaxRhtCode();
        RegionVo rootRegion = new RegionVo();
        rootRegion.setId(RegionVo.ROOT_ID);
        rootRegion.setName("所有");
        rootRegion.setParentId(RegionVo.ROOT_PARENTID);
        rootRegion.setLft(1);
        rootRegion.setRht(maxRhtCode == null ? 2 : maxRhtCode + 1);
        List<RegionVo> regionList = regionMapper.getRegionListForTree(rootRegion.getLft(), rootRegion.getRht(), isActive);
        //将虚拟的root节点加入到catalogList中
        regionList.add(rootRegion);
        for (RegionVo regionVo : regionList) {
            idKeyMap.put(regionVo.getId(), regionVo);
        }
        for (RegionVo regionVo : regionList) {
            Long parentId = regionVo.getParentId();
            RegionVo parent = idKeyMap.get(parentId);
            if (parent != null) {
                regionVo.setParent(parent);
            }
        }
        return rootRegion.getChildren();
    }

    @Override
    public String getToken() {
        return "/region/tree/search";
    }
}
