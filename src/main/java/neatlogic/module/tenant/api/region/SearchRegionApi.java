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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.region.RegionMapper;
import neatlogic.framework.dto.region.RegionVo;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.service.RegionService;
import neatlogic.framework.util.TableResultUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = NoAuth.class)
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
