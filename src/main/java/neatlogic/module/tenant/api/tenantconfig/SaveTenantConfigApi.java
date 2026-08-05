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
import neatlogic.framework.common.util.RC4Util;
import neatlogic.framework.config.ITenantConfig;
import neatlogic.framework.config.TenantConfigFactory;
import neatlogic.framework.dao.mapper.ConfigMapper;
import neatlogic.framework.dto.ConfigVo;
import neatlogic.framework.exception.tenantconfig.TenantConfigNotFoundException;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.param.validate.core.ApiParamValidatorBase;
import neatlogic.framework.param.validate.core.ParamValidatorFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.lang3.StringUtils;
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
        // 系统参数留空表示删除租户覆盖配置，后续使用参数的代码内置默认值。
        if (StringUtils.isBlank(configVo.getValue())) {
            configMapper.deleteConfigByKey(configVo.getKey());
            return null;
        }
        ApiParamType type = tenantConfig.getType();
        if (type != null) {
            ApiParamValidatorBase authInstance = ParamValidatorFactory.getAuthInstance(type);
            if (authInstance != null) {
                if (!authInstance.validate(configVo.getValue(), tenantConfig.getRule())) {
                    throw new ParamIrregularException("value(值)");
                }
            }
            if (type == ApiParamType.PASSWORD && configVo.getValue() != null) {
                configVo.setValue(RC4Util.encrypt(configVo.getValue()));
            }
        }
        configMapper.insertConfig(configVo);
        return null;
    }

    @Override
    public String getToken() {
        return "tenantconfig/save";
    }
}
