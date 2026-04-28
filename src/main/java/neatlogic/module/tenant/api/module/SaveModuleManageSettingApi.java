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
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.MODULE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.module.ModuleManageSettingVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.SnowflakeUtil;
import neatlogic.module.tenant.dao.mapper.ModuleManageSettingMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@AuthAction(action = MODULE_MODIFY.class)
@OperationType(type = OperationTypeEnum.OPERATE)
@Transactional
public class SaveModuleManageSettingApi extends PrivateApiComponentBase {

    @Resource
    private ModuleManageSettingMapper moduleManageSettingMapper;

    @Override
    public String getToken() {
        return "module/manage/setting/save";
    }

    @Override
    public String getName() {
        return "nmtam.savemodulemanagesettingapi.getname";
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "common.id"),
            @Param(name = "config", type = ApiParamType.JSONOBJECT, isRequired = true, desc = "common.config")
    })
    @Output({})
    @Description(desc = "nmtam.savemodulemanagesettingapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        JSONObject config = jsonObj.getJSONObject("config");
        ModuleManageSettingVo moduleManageSettingVo = new ModuleManageSettingVo();
        if (id != null) {
            moduleManageSettingVo.setId(id);
        } else {
            ModuleManageSettingVo currentSetting = moduleManageSettingMapper.getModuleManageSetting();
            if (currentSetting != null) {
                moduleManageSettingVo.setId(currentSetting.getId());
            } else {
                moduleManageSettingVo.setId(SnowflakeUtil.uniqueLong());
            }
        }
        moduleManageSettingVo.setConfig(config);
        moduleManageSettingMapper.insertModuleManageSetting(moduleManageSettingVo);
        return null;
    }
}
