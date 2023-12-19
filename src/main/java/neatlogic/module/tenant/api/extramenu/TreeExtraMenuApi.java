/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

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
