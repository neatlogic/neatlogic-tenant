/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/
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
