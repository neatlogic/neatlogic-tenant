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
    @Description(desc = "获取模块列表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<ModuleGroupVo> moduleGroupList = TenantContext.get().getActiveModuleGroupList();
        moduleGroupList.sort(Comparator.comparing(ModuleGroupVo::getGroupSort));
        String tenantUuid = TenantContext.get().getTenantUuid();
        List<TenantModuleVo> tenantModuleVos = tenantMapper.getTenantModuleByTenantUuid(tenantUuid);
        Map<String, String> moduleVersionMap = tenantModuleVos.stream().collect(Collectors.toMap(TenantModuleVo::getModuleId, o -> o.getVersion() == null ? StringUtils.EMPTY : o.getVersion()));
        moduleGroupList.forEach(o -> o.getModuleList().forEach(e -> e.setChangelogVersion(moduleVersionMap.get(e.getId()))));
        return moduleGroupList;
    }
}
