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

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
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
@AuthAction(action = NoAuth.class)
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
