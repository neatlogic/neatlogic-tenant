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
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.dao.mapper.ExtraMenuMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ListExtraMenuItemApi extends PrivateApiComponentBase {

    @Resource
    private ExtraMenuMapper extraMenuMapper;


    @Override
    public String getName() {
        return "获取附加菜单列表";
    }

    @Input({@Param(name = "type", type = ApiParamType.INTEGER, rule = "0,1", desc = "类型"),
            @Param(name = "openType", type = ApiParamType.STRING, rule = "window,iframe", desc = "打开方式")})
    @Output({@Param(name = "Return", type = ApiParamType.JSONOBJECT, explode = ExtraMenuVo[].class)})
    @Description(desc = "获取附加菜单列表")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Integer type = paramObj.getInteger("type");
        String openType = paramObj.getString("openType");
        AuthenticationInfoVo authenticationInfoVo = UserContext.get().getAuthenticationInfoVo();
        // 已授权的节点
        List<Long> idList = extraMenuMapper.getAuthorizedExtraMenuIdList(UserContext.get().getUserUuid(true),
                authenticationInfoVo.getTeamUuidList(), authenticationInfoVo.getRoleUuidList());
        if (CollectionUtils.isEmpty(idList)) {
            return null;
        }
        List<ExtraMenuVo> list = extraMenuMapper.getExtraMenuByIdList(idList);
        if (type != null) {
            list.removeIf(d -> !Objects.equals(d.getType(), type));
        }
        if (StringUtils.isNotEmpty(openType)) {
            list.removeIf(d -> !Objects.equals(d.getOpenType(), openType));
        }
        return list;
    }


    @Override
    public String getToken() {
        return "/extramenu/item/list";
    }
}
