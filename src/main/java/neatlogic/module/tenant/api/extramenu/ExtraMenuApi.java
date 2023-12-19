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
public class ExtraMenuApi extends PrivateApiComponentBase {

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
