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
import neatlogic.framework.extramenu.constvalue.ExtraMenuType;
import neatlogic.framework.extramenu.exception.ExtraMenuRootNotAllowedException;
import neatlogic.framework.lrcode.LRCodeManager;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.dao.mapper.ExtraMenuMapper;
import neatlogic.framework.extramenu.dto.ExtraMenuVo;
import neatlogic.framework.extramenu.exception.ExtraMenuExistChildrenException;
import neatlogic.framework.extramenu.exception.ExtraMenuNotFoundException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
@AuthAction(action = EXTRA_MENU_MODIFY.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class DeleteExtraMenuApi extends PrivateApiComponentBase {

    @Resource
    private ExtraMenuMapper extraMenuMapper;

    @Override
    public String getName() {
        return "nmtae.extramenudeleteapi.getname";
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "common.id")})
    @Description(desc = "nmtae.extramenudeleteapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long id = paramObj.getLong("id");
        ExtraMenuVo vo = extraMenuMapper.getExtraMenuById(id);
        if (vo == null) {
            throw new ExtraMenuNotFoundException(id);
        }
        if (ExtraMenuVo.ROOT_ID.equals(vo.getParentId())) {
            throw new ExtraMenuRootNotAllowedException();
        }
        if (ExtraMenuType.DIRECTORY.getType() == vo.getType()) {
            List<ExtraMenuVo> list = extraMenuMapper.getExtraMenuForTree(vo.getLft(), vo.getRht());
            if (CollectionUtils.isNotEmpty(list) && list.size() > 1) {
                // 存在子节点
                throw new ExtraMenuExistChildrenException(vo.getName());
            }
        }
        LRCodeManager.beforeDeleteTreeNode("extramenu", "id", "parent_id", id);
        extraMenuMapper.deleteExtraMenuAuthorityByMenuId(id);
        extraMenuMapper.deleteExtraMenuById(id);
        return null;
    }

    @Override
    public String getToken() {
        return "/extramenu/delete";
    }
}
