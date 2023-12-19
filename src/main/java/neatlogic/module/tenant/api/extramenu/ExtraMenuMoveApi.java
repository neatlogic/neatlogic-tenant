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
public class ExtraMenuMoveApi extends PrivateApiComponentBase {

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
