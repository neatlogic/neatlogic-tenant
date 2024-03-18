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
import neatlogic.framework.extramenu.dto.ExtraMenuVo;
import neatlogic.framework.extramenu.exception.ExtraMenuNameRepeatException;
import neatlogic.framework.extramenu.exception.ExtraMenuNotAllowedAddException;
import neatlogic.framework.extramenu.exception.ExtraMenuRootException;
import neatlogic.framework.lrcode.LRCodeManager;
import neatlogic.framework.lrcode.constvalue.MoveType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.dao.mapper.ExtraMenuMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service

@AuthAction(action = EXTRA_MENU_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
@Transactional
public class MoveExtraMenuApi extends PrivateApiComponentBase {

    @Resource
    private ExtraMenuMapper extraMenuMapper;

    @Override
    public String getName() {
        return "nmtae.extramenumoveapi.getname";
    }

    @Input({
        @Param(name = "id", type = ApiParamType.LONG, isRequired = true,
            desc = "nmtae.extramenumoveapi.input.param.id.desc"),
        @Param(name = "targetId", type = ApiParamType.LONG, isRequired = true,
            desc = "nmtae.extramenumoveapi.input.param.targetid.desc"),
        @Param(name = "moveType", type = ApiParamType.ENUM, rule = "inner,prev,next", isRequired = true,
            desc = "nmtae.extramenumoveapi.input.param.movetype.desc")})
    @Description(desc = "nmtae.extramenumoveapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long id = paramObj.getLong("id");
        Long targetId = paramObj.getLong("targetId");
        String moveType = paramObj.getString("moveType");
        LRCodeManager.moveTreeNode("extramenu", "id", "parent_id", id, MoveType.getMoveType(moveType), targetId);

        ExtraMenuVo vo = extraMenuMapper.getExtraMenuById(id);
        if (ExtraMenuVo.ROOT_ID.equals(vo.getParentId())) {
            if (extraMenuMapper.checkExtraMenuRootCount(ExtraMenuVo.ROOT_ID) > 1) {
                throw new ExtraMenuRootException();
            }
        } else {
            // 判断移动后的父节点是否为目录节点
            ExtraMenuVo parentVo = extraMenuMapper.getExtraMenuById(vo.getParentId());
            if (parentVo.getType().intValue() != ExtraMenuType.DIRECTORY.getType()) {
                throw new ExtraMenuNotAllowedAddException();
            }
        }
        // 判断移动后是否在同一父目录下节点是否存在相同名称
        if (extraMenuMapper.checkExtraMenuNameIsRepeat(vo) > 0) {
            throw new ExtraMenuNameRepeatException(vo.getName());
        }
        return null;
    }

    @Override
    public String getToken() {
        return "/extramenu/move";
    }
}
