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
import neatlogic.framework.dto.AuthorityVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.dao.mapper.ExtraMenuMapper;
import neatlogic.framework.extramenu.dto.ExtraMenuVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service

@AuthAction(action = EXTRA_MENU_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetExtraMenuApi extends PrivateApiComponentBase {

    @Resource
    private ExtraMenuMapper extraMenuMapper;

    @Override
    public String getName() {
        return "nmtae.extramenugetapi.getname";
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "common.id")})
    @Output({@Param(name = "Return", type = ApiParamType.JSONOBJECT, explode = ExtraMenuVo.class)})
    @Description(desc = "nmtae.extramenugetapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        ExtraMenuVo vo = extraMenuMapper.getExtraMenuById(paramObj.getLong("id"));
        if (vo != null) {
            List<AuthorityVo> authorityVoList = extraMenuMapper.getExtraMenuAuthorityListByMenuId(vo.getId());
            vo.setAuthorityVoList(authorityVoList);
        }
        return vo;
    }

    @Override
    public String getToken() {
        return "/extramenu/get";
    }
}
