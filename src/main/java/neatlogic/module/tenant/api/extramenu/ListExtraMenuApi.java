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
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ListExtraMenuApi extends PrivateApiComponentBase {

    @Resource
    private ExtraMenuMapper extraMenuMapper;


    @Override
    public String getName() {
        return "nmtae.extramenuapi.getname";
    }

    @Output({@Param(name = "Return", type = ApiParamType.JSONOBJECT, explode = ExtraMenuVo[].class)})
    @Description(desc = "nmtae.extramenuapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        AuthenticationInfoVo authenticationInfoVo = UserContext.get().getAuthenticationInfoVo();
        // 已授权的节点
        List<Long> idList = extraMenuMapper.getAuthorizedExtraMenuIdList(UserContext.get().getUserUuid(true),
                authenticationInfoVo.getTeamUuidList(), authenticationInfoVo.getRoleUuidList());
        if (CollectionUtils.isEmpty(idList)) {
            return null;
        }
        List<ExtraMenuVo> list = extraMenuMapper.getExtraMenuByIdList(idList);
        //变回树形结构
        Map<Long, ExtraMenuVo> map = new HashMap<>();
        for (ExtraMenuVo extraMenuVo : list) {
            map.put(extraMenuVo.getId(), extraMenuVo);
        }
        List<ExtraMenuVo> resultList = new ArrayList<>();
        for (ExtraMenuVo extraMenuVo : list) {
            if (map.containsKey(extraMenuVo.getParentId())) {
                extraMenuVo.setParent(map.get(extraMenuVo.getParentId()));
            }
            if (extraMenuVo.getParentId().equals(0L)) {
                resultList.add(extraMenuVo);
            }
        }
        //去掉没有子菜单的父菜单
        for (ExtraMenuVo extraMenuVo : resultList) {
            removeEmptyMenu(extraMenuVo);
        }
        resultList.removeIf(d -> CollectionUtils.isEmpty(d.getChildren()));
        return resultList;
    }

    private void removeEmptyMenu(ExtraMenuVo extraMenuVo) {
        if (CollectionUtils.isNotEmpty(extraMenuVo.getChildren())) {
            Iterator<ExtraMenuVo> iterator = extraMenuVo.getChildren().iterator();
            while (iterator.hasNext()) {
                ExtraMenuVo child = iterator.next();
                removeEmptyMenu(child);
                if (child.getType() == 0 && CollectionUtils.isEmpty(child.getChildren())) {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public String getToken() {
        return "/extramenu/list";
    }
}
