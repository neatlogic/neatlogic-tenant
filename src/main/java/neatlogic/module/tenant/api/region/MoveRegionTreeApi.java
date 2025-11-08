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
import neatlogic.framework.auth.label.REGION_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.region.RegionMapper;
import neatlogic.framework.dto.region.RegionVo;
import neatlogic.framework.exception.region.RegionNameRepeatException;
import neatlogic.framework.exception.region.RegionNotFoundException;
import neatlogic.framework.lrcode.LRCodeManager;
import neatlogic.framework.lrcode.constvalue.MoveType;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@AuthAction(action = REGION_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
@Transactional
public class MoveRegionTreeApi extends PrivateApiComponentBase {
    @Resource
    RegionMapper regionMapper;

    @Override
    public String getName() {
        return "nmtar.moveregiontreeapi.getname";
    }


    @Input({
            @Param(name = "id", type = ApiParamType.STRING, isRequired = true, desc = "nmtar.moveregiontreeapi.input.param.desc.id"),
            @Param(name = "targetId", type = ApiParamType.STRING, isRequired = true, desc = "nmtar.moveregiontreeapi.input.param.desc.targetid"),
            @Param(name = "moveType", type = ApiParamType.ENUM, rule = "inner,prev,next", isRequired = true, desc = "nmtae.extramenumoveapi.input.param.movetype.desc")
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long id = paramObj.getLong("id");
        RegionVo regionVo = regionMapper.getRegionById(id);
        if(regionVo == null){
            throw new RegionNotFoundException(id);
        }
        Long targetId = paramObj.getLong("targetId");
        RegionVo targetRegionVo = regionMapper.getRegionById(targetId);
        if(targetRegionVo == null){
            throw new RegionNotFoundException(targetId);
        }
        String moveType = paramObj.getString("moveType");
        int[] lftRht = LRCodeManager.moveTreeNode("region", "id", "parent_id", id, MoveType.getMoveType(moveType), targetId);
        if (!targetId.equals(regionVo.getParentId()) && lftRht != null) {
            regionMapper.updateUpwardIdPathByLftRht(lftRht[0], lftRht[1]);
            regionMapper.updateUpwardNamePathByLftRht(lftRht[0], lftRht[1]);
        }
        Long parentId = regionMapper.getParentIdById(id);
        regionVo.setParentId(parentId);
        //判断移动后相同目录下是否有同名目录
        if(regionMapper.checkRegionNameIsRepeat(regionVo) > 0) {
            throw new RegionNameRepeatException(regionVo.getName());
        }
        return null;
    }

    @Override
    public String getToken() {
        return "/region/tree/move";
    }
}
