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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.region.RegionMapper;
import neatlogic.framework.dto.region.RegionVo;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.framework.service.RegionService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchRegionApi extends PrivateApiComponentBase {
    @Resource
    RegionMapper regionMapper;

    @Resource
    TeamMapper teamMapper;

    @Resource
    RegionService regionService;

    @Override
    public String getName() {
        return "nmtar.searchregionapi.getname";
    }


    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword", xss = true, help = "名称"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize"),
            @Param(name = "defaultValue", type = ApiParamType.JSONARRAY, desc = "common.defaultvalue"),
            @Param(name = "owner", type = ApiParamType.STRING, desc = "nmtc.regiontype.owner")
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        RegionVo region = JSON.toJavaObject(paramObj, RegionVo.class);
        JSONArray defaultValue = region.getDefaultValue();
        List<RegionVo> regionList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(defaultValue)) {
            List<Long> idList = defaultValue.toJavaList(Long.class);
            regionList = regionMapper.getRegionListByIdList(idList);
            return TableResultUtil.getResult(regionList);
        } else {
            String owner = paramObj.getString("owner");
            //根据上报人获取地域
            if (StringUtils.isNotBlank(owner)) {
                List<Long> regionIdList = regionService.getRegionIdListByUserUuid(owner);
                if (CollectionUtils.isEmpty(regionIdList)) {
                    return TableResultUtil.getResult(regionList, region);
                }
                region.setIdList(regionIdList);
            }
            int rowNum = regionMapper.searchRegionCount(region);
            region.setRowNum(rowNum);
            if (rowNum > 0) {
                regionList = regionMapper.searchRegion(region);
            }
        }
        return TableResultUtil.getResult(regionList, region);
    }

    @Override
    public String getToken() {
        return "/region/search";
    }


}
