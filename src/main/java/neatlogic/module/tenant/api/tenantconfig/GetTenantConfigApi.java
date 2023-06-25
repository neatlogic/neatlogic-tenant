/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.tenantconfig;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.TENANT_CONFIG_BASE;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.config.ITenantConfig;
import neatlogic.framework.config.TenantConfigFactory;
import neatlogic.framework.dao.mapper.ConfigMapper;
import neatlogic.framework.dto.ConfigVo;
import neatlogic.framework.exception.tenantconfig.TenantConfigNotFoundException;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = TENANT_CONFIG_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetTenantConfigApi extends PrivateApiComponentBase {

    @Resource
    private ConfigMapper configMapper;

    @Override
    public String getName() {
        return "获取当前租户配置信息";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "key", type = ApiParamType.STRING, isRequired = true, desc = "配置信息的唯一标识")
    })
    @Output({
          @Param(explode = ConfigVo.class)
    })
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
        return configVo;
    }

    @Override
    public String getToken() {
        return "tenantconfig/get";
    }
}
