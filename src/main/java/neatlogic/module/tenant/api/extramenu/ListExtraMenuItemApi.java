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
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.extramenu.dto.ExtraMenuVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.dao.mapper.ExtraMenuMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ListExtraMenuItemApi extends PrivateApiComponentBase {

    @Resource
    private ExtraMenuMapper extraMenuMapper;


    @Override
    public String getName() {
        return "获取附加菜单列表";
    }

    @Input({@Param(name = "type", type = ApiParamType.INTEGER, rule = "0,1", desc = "类型"),
            @Param(name = "openType", type = ApiParamType.STRING, rule = "window,iframe", desc = "打开方式")})
    @Output({@Param(name = "Return", type = ApiParamType.JSONOBJECT, explode = ExtraMenuVo[].class)})
    @Description(desc = "获取附加菜单列表")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Integer type = paramObj.getInteger("type");
        String openType = paramObj.getString("openType");
        AuthenticationInfoVo authenticationInfoVo = UserContext.get().getAuthenticationInfoVo();
        // 已授权的节点
        List<Long> idList = extraMenuMapper.getAuthorizedExtraMenuIdList(UserContext.get().getUserUuid(true),
                authenticationInfoVo.getTeamUuidList(), authenticationInfoVo.getRoleUuidList());
        if (CollectionUtils.isEmpty(idList)) {
            return null;
        }
        List<ExtraMenuVo> list = extraMenuMapper.getExtraMenuByIdList(idList);
        if (type != null) {
            list.removeIf(d -> !Objects.equals(d.getType(), type));
        }
        if (StringUtils.isNotEmpty(openType)) {
            list.removeIf(d -> !Objects.equals(d.getOpenType(), openType));
        }
        return list;
    }


    @Override
    public String getToken() {
        return "/extramenu/item/list";
    }
}
