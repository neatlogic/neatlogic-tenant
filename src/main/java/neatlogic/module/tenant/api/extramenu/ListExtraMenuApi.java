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

package neatlogic.module.tenant.api.extramenu;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.extramenu.dto.ExtraMenuVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.dao.mapper.ExtraMenuMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ListExtraMenuApi extends PrivateApiComponentBase {

    @Resource
    private ExtraMenuMapper extraMenuMapper;


    @Override
    public String getName() {
        return "nmtae.extramenuapi.getname";
    }

    @Output({@Param(name = "Return", type = ApiParamType.JSONOBJECT, explode = ExtraMenuVo[].class)})
    @Description(desc = "nmtae.extramenuapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        AuthenticationInfoVo authenticationInfoVo = UserContext.get().getAuthenticationInfoVo();
        // 已授权的节点
        List<Long> idList = extraMenuMapper.getAuthorizedExtraMenuIdList(UserContext.get().getUserUuid(true),
                authenticationInfoVo.getTeamUuidList(), authenticationInfoVo.getRoleUuidList());
        if (CollectionUtils.isEmpty(idList)) {
            return null;
        }
        List<ExtraMenuVo> list = extraMenuMapper.getExtraMenuByIdList(idList);
        //变回树形结构
        Map<Long, ExtraMenuVo> map = new HashMap<>();
        for (ExtraMenuVo extraMenuVo : list) {
            map.put(extraMenuVo.getId(), extraMenuVo);
        }
        List<ExtraMenuVo> resultList = new ArrayList<>();
        for (ExtraMenuVo extraMenuVo : list) {
            if (map.containsKey(extraMenuVo.getParentId())) {
                extraMenuVo.setParent(map.get(extraMenuVo.getParentId()));
            }
            if (extraMenuVo.getParentId().equals(0L)) {
                resultList.add(extraMenuVo);
            }
        }
        //去掉没有子菜单的父菜单
        for (ExtraMenuVo extraMenuVo : resultList) {
            removeEmptyMenu(extraMenuVo);
        }
        resultList.removeIf(d -> CollectionUtils.isEmpty(d.getChildren()));
        return resultList;
    }

    private void removeEmptyMenu(ExtraMenuVo extraMenuVo) {
        if (CollectionUtils.isNotEmpty(extraMenuVo.getChildren())) {
            Iterator<ExtraMenuVo> iterator = extraMenuVo.getChildren().iterator();
            while (iterator.hasNext()) {
                ExtraMenuVo child = iterator.next();
                removeEmptyMenu(child);
                if (child.getType() == 0 && CollectionUtils.isEmpty(child.getChildren())) {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public String getToken() {
        return "/extramenu/list";
    }
}
