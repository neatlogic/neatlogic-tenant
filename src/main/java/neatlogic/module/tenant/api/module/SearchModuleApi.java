/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

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

package neatlogic.module.tenant.api.module;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.MODULE_MODIFY;
import neatlogic.framework.dao.mapper.TenantMapper;
import neatlogic.framework.dto.TenantModuleVo;
import neatlogic.framework.dto.module.ModuleGroupVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AuthAction(action = MODULE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchModuleApi extends PrivateApiComponentBase {

    @Resource
    private TenantMapper tenantMapper;

    @Override
    public String getToken() {
        return "/module/search";
    }

    @Override
    public String getName() {
        return "获取模块列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({@Param(explode = ModuleGroupVo[].class)})
    @Description(desc = "获取模块列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<ModuleGroupVo> moduleGroupList = TenantContext.get().getActiveModuleGroupList();
        moduleGroupList.sort(Comparator.comparing(ModuleGroupVo::getGroupSort));
        String tenantUuid = TenantContext.get().getTenantUuid();
        TenantContext.get().setUseDefaultDatasource(true);
        List<TenantModuleVo> tenantModuleVos = tenantMapper.getTenantModuleByTenantUuid(tenantUuid);
        Map<String, String> moduleVersionMap = tenantModuleVos.stream().collect(Collectors.toMap(TenantModuleVo::getModuleId, o -> o.getVersion() == null ? StringUtils.EMPTY : o.getVersion()));
        moduleGroupList.forEach(o -> {
            o.getModuleList().forEach(e -> {
                e.setChangelogVersion(moduleVersionMap.get(e.getId()));
            });
        });
        TenantContext.get().setUseDefaultDatasource(false);
        return moduleGroupList;
    }
}
