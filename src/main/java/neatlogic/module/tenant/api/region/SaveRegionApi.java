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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.REGION_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.region.RegionMapper;
import neatlogic.framework.dto.region.RegionVo;
import neatlogic.framework.exception.region.RegionNotFoundException;
import neatlogic.framework.lrcode.LRCodeManager;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AuthAction(action = REGION_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
@Transactional
public class SaveRegionApi extends PrivateApiComponentBase {
    @Resource
    RegionMapper regionMapper;

    @Override
    public String getName() {
        return "nmtar.saveregionapi.getname";
    }


    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "common.id", help = "id"),
            @Param(name = "name", type = ApiParamType.STRING, desc = "common.name", xss = true, help = "名称"),
            @Param(name = "isActive", type = ApiParamType.INTEGER, desc = "common.isactive", help = "是否激活"),
            @Param(name = "parentId", type = ApiParamType.LONG, desc = "common.parentid", help = "父地域id"),
            @Param(name = "workTimeUuid", type = ApiParamType.STRING, desc = "common.worktimeuuid", help = "服务时间窗口uuid"),

    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long id = paramObj.getLong("id");
        if (id != null) {
            RegionVo regionVo = regionMapper.getRegionById(id);
            if (regionVo == null) {
                throw new RegionNotFoundException(id);
            }
        }
        RegionVo region = JSON.toJavaObject(paramObj, RegionVo.class);
        if(id == null) {
            int lft = LRCodeManager.beforeAddTreeNode("region", "id", "parent_id", region.getParentId());
            region.setLft(lft);
            region.setRht(lft + 1);
        }
        List<Long> upwardRegionIdList = new ArrayList<>();
        List<String> upwardRegionNameList = new ArrayList<>();
        List<RegionVo> upwardRegionList = regionMapper.getAncestorsAndSelfByLftRht(region.getLft(), region.getRht());
        for (RegionVo upwardRegion : upwardRegionList) {
            upwardRegionIdList.add(upwardRegion.getId());
            upwardRegionNameList.add(upwardRegion.getName());
        }
        upwardRegionIdList.add(region.getId());
        upwardRegionNameList.add(region.getName());
        region.setUpwardIdPath(upwardRegionIdList.stream().map(Object::toString).collect(Collectors.joining(",")));
        region.setUpwardNamePath(String.join("/", upwardRegionNameList));
        regionMapper.insertRegion(region);
        return region.getId();
    }

    @Override
    public String getToken() {
        return "/region/save";
    }
}
