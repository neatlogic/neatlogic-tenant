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
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.extramenu.dto.ExtraMenuVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.dao.mapper.ExtraMenuMapper;
import neatlogic.module.tenant.service.extramenu.ExtraMenuService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class ListExtraMenuApi extends PrivateApiComponentBase {

    @Resource
    private ExtraMenuMapper extraMenuMapper;

    @Resource
    private ExtraMenuService extraMenuService;

    @Override
    public String getName() {
        return "nmtae.extramenuapi.getname";
    }

    @Output({@Param(name = "Return", type = ApiParamType.JSONOBJECT, explode = ExtraMenuVo.class)})
    @Description(desc = "nmtae.extramenuapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        ExtraMenuVo root = extraMenuService.buildRootExtraMenu();
        AuthenticationInfoVo authenticationInfoVo = UserContext.get().getAuthenticationInfoVo();
        // 已授权的节点
        List<Long> idList = extraMenuMapper.getAuthorizedExtraMenuIdList(UserContext.get().getUserUuid(true),
            authenticationInfoVo.getTeamUuidList(), authenticationInfoVo.getRoleUuidList());
        if (CollectionUtils.isEmpty(idList)) {
            return null;
        }
        idList.add(ExtraMenuVo.ROOT_ID);
        List<ExtraMenuVo> list = extraMenuMapper.getExtraMenuForTree(root.getLft(), root.getRht());
        if (!list.isEmpty()) {
            Map<Long, ExtraMenuVo> map = new LinkedHashMap<>();
            list.add(root);
            for (ExtraMenuVo vo : list) {
                // 过滤无权限以及未激活的节点
                if (!idList.contains(vo.getId()) || vo.getIsActive().intValue() == 0) {
                    continue;
                }
                map.put(vo.getId(), vo);
            }
            for (Long id : map.keySet()) {
                ExtraMenuVo parent = map.get(map.get(id).getParentId());
                map.get(id).setParent(parent);
            }
        }
        if (root.getChildren() != null && root.getChildren().size() > 0) {
            return extraMenuService.removeEmptyDirectory(root.getChildren().get(0));
        } else {
            return null;
        }
    }

    @Override
    public String getToken() {
        return "/extramenu/list";
    }
}
