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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.EXTRA_MENU_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.extramenu.dto.ExtraMenuVo;
import neatlogic.framework.lrcode.LRCodeManager;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.dao.mapper.ExtraMenuMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service

@AuthAction(action = EXTRA_MENU_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
@Transactional
public class MoveExtraMenuApi extends PrivateApiComponentBase {

    @Resource
    private ExtraMenuMapper extraMenuMapper;

    @Override
    public String getName() {
        return "更新菜单位置";
    }

    @Input({
            @Param(name = "menuList", type = ApiParamType.JSONARRAY, isRequired = true,
                    desc = "节点列表")
    })
    @Description(desc = "更新菜单位置")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONArray menuList = paramObj.getJSONArray("menuList");
        List<ExtraMenuVo> extraMenuList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(menuList)) {
            for (int i = 0; i < menuList.size(); i++) {
                ExtraMenuVo menuVo = JSON.toJavaObject(menuList.getJSONObject(i), ExtraMenuVo.class);
                extraMenuList.add(menuVo);
            }
            updateMenu(extraMenuList, 0, 0L);
            //重建所有左右编码，性能差点但可靠
            LRCodeManager.rebuildLeftRightCodeOrderBySortKey("extramenu", "id", "parent_id", "sort");
        }

        return null;
    }

    private void updateMenu(List<ExtraMenuVo> extraMenuList, Integer sort, Long parentId) {
        for (ExtraMenuVo menuVo : extraMenuList) {
            sort += 1;
            menuVo.setSort(sort);
            menuVo.setParentId(parentId);
            extraMenuMapper.updateExtraMenuSort(menuVo);
            if (CollectionUtils.isNotEmpty(menuVo.getChildren())) {
                updateMenu(menuVo.getChildren(), sort, menuVo.getId());
            }
        }
    }

    @Override
    public String getToken() {
        return "/extramenu/move";
    }
}
