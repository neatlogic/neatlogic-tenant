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
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.EXTRA_MENU_MODIFY;
import neatlogic.framework.extramenu.dto.ExtraMenuVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.dao.mapper.ExtraMenuMapper;
import neatlogic.module.tenant.service.extramenu.ExtraMenuService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service

@AuthAction(action = EXTRA_MENU_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class TreeExtraMenuApi extends PrivateApiComponentBase {

    @Resource
    private ExtraMenuMapper extraMenuMapper;
    @Resource
    private ExtraMenuService extraMenuService;

    @Override
    public String getName() {
        return "nmtae.extramenutreeapi.getname";
    }

    @Output({@Param(name = "Return", explode = ExtraMenuVo[].class)})
    @Description(desc = "nmtae.extramenutreeapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        ExtraMenuVo root = extraMenuService.buildRootExtraMenu();
        List<ExtraMenuVo> list = extraMenuMapper.getExtraMenuForTree(root.getLft(), root.getRht());
        if (CollectionUtils.isNotEmpty(list)) {
            Map<Long, ExtraMenuVo> map = new HashMap<>();
            list.add(root);
            list.forEach(o -> map.put(o.getId(), o));
            for (ExtraMenuVo vo : list) {
                ExtraMenuVo parent = map.get(vo.getParentId());
                vo.setParent(parent);
            }
        }
        if (CollectionUtils.isNotEmpty(root.getChildren())) {
            return root.getChildren();
        } else {
            return null;
        }
    }

    @Override
    public String getToken() {
        return "/extramenu/tree";
    }
}
