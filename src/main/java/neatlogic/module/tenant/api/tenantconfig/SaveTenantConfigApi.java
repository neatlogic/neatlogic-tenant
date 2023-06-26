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
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.config.ITenantConfig;
import neatlogic.framework.config.TenantConfigFactory;
import neatlogic.framework.dao.mapper.ConfigMapper;
import neatlogic.framework.dto.ConfigVo;
import neatlogic.framework.exception.tenantconfig.TenantConfigNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class SaveTenantConfigApi extends PrivateApiComponentBase {

    @Resource
    private ConfigMapper configMapper;

    @Override
    public String getName() {
        return "nmtat.savetenantconfigapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "key", type = ApiParamType.STRING, isRequired = true, desc = "common.key"),
            @Param(name = "value", type = ApiParamType.STRING, desc = "common.value"),
            @Param(name = "description", type = ApiParamType.STRING, desc = "common.description"),
    })
    @Output({})
    @Description(desc = "nmtat.savetenantconfigapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        ConfigVo configVo = paramObj.toJavaObject(ConfigVo.class);
        ITenantConfig tenantConfig = TenantConfigFactory.getTenantConfigByKey(configVo.getKey());
        if (tenantConfig == null) {
            throw new TenantConfigNotFoundException(configVo.getKey());
        }
        configMapper.insertConfig(configVo);
        return null;
    }

    @Override
    public String getToken() {
        return "tenantconfig/save";
    }
}
