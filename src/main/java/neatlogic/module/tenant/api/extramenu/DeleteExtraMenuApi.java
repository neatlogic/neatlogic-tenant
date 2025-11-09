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
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.extramenu.constvalue.ExtraMenuType;
import neatlogic.framework.extramenu.dto.ExtraMenuVo;
import neatlogic.framework.extramenu.exception.ExtraMenuExistChildrenException;
import neatlogic.framework.extramenu.exception.ExtraMenuNotFoundException;
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
        if (ExtraMenuType.DIRECTORY.getType() == vo.getType()) {
            List<ExtraMenuVo> list = extraMenuMapper.getExtraMenuForTree(vo.getLft(), vo.getRht());
            if (CollectionUtils.isNotEmpty(list) && list.size() > 1) {
                // 存在子节点
                throw new ExtraMenuExistChildrenException(vo.getName());
            }
        }
        extraMenuMapper.deleteExtraMenuAuthorityByMenuId(id);
        extraMenuMapper.deleteExtraMenuById(id);
        //重建所有左右编码，性能差点但可靠
        LRCodeManager.rebuildLeftRightCodeOrderBySortKey("extramenu", "id", "parent_id", "sort");
        return null;
    }

    @Override
    public String getToken() {
        return "/extramenu/delete";
    }
}
