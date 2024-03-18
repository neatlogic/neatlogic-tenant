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

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.EXTRA_MENU_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.dao.mapper.ExtraMenuMapper;
import neatlogic.framework.extramenu.dto.ExtraMenuVo;
import neatlogic.module.tenant.service.extramenu.ExtraMenuService;
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

    @Output({@Param(name = "Return", type = ApiParamType.JSONOBJECT, explode = ExtraMenuVo.class)})
    @Description(desc = "nmtae.extramenutreeapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        ExtraMenuVo root = extraMenuService.buildRootExtraMenu();
        List<ExtraMenuVo> list = extraMenuMapper.getExtraMenuForTree(root.getLft(), root.getRht());
        if (!list.isEmpty()) {
            Map<Long, ExtraMenuVo> map = new HashMap<>();
            list.add(root);
            list.forEach(o -> map.put(o.getId(), o));
            for (ExtraMenuVo vo : list) {
                ExtraMenuVo parent = map.get(vo.getParentId());
                vo.setParent(parent);
            }
        }
        if (root.getChildren() != null && root.getChildren().size() > 0) {
            return root.getChildren().get(0);
        } else {
            return null;
        }
    }

    @Override
    public String getToken() {
        return "/extramenu/tree";
    }
}
