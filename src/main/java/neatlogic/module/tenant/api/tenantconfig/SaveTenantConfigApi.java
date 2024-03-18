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
