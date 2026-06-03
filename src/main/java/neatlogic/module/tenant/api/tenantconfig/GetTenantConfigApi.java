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

package neatlogic.module.tenant.api.tenantconfig;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.ModuleUtil;
import neatlogic.framework.config.ITenantConfig;
import neatlogic.framework.config.TenantConfigFactory;
import neatlogic.framework.dao.mapper.ConfigMapper;
import neatlogic.framework.dto.ConfigVo;
import neatlogic.framework.dto.module.ModuleGroupVo;
import neatlogic.framework.exception.tenantconfig.TenantConfigNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetTenantConfigApi extends PrivateApiComponentBase {

    @Resource
    private ConfigMapper configMapper;

    @Override
    public String getName() {
        return "nmtat.gettenantconfigapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "key", type = ApiParamType.STRING, isRequired = true, desc = "common.key")
    })
    @Output({
          @Param(explode = ConfigVo.class)
    })
    @Description(desc = "nmtat.gettenantconfigapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String key = paramObj.getString("key");
        ITenantConfig tenantConfig = TenantConfigFactory.getTenantConfigByKey(key);
        if (tenantConfig == null) {
            throw new TenantConfigNotFoundException(key);
        }
        ConfigVo configVo = configMapper.getConfigByKey(key);
        if (configVo == null) {
            configVo = new ConfigVo();
            configVo.setKey(tenantConfig.getKey());
            configVo.setValue(tenantConfig.getValue());
        }
        configVo.setDescription(tenantConfig.getDescription());
        ApiParamType type = tenantConfig.getType();
        if (type != null) {
            configVo.setType(type.getText());
        }
        String moduleGroup = tenantConfig.getModuleGroup();
        ModuleGroupVo moduleGroupVo = StringUtils.isBlank(moduleGroup) ? null : ModuleUtil.getModuleGroup(moduleGroup);
        configVo.setModuleGroup(moduleGroup);
        configVo.setModuleGroupName(moduleGroupVo == null ? moduleGroup : moduleGroupVo.getGroupName());
        configVo.setModuleGroupSort(moduleGroupVo == null ? null : moduleGroupVo.getGroupSort());
        return configVo;
    }

    @Override
    public String getToken() {
        return "tenantconfig/get";
    }
}
