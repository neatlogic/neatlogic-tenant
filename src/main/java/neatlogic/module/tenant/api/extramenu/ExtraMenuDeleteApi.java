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
public class ExtraMenuDeleteApi extends PrivateApiComponentBase {

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
