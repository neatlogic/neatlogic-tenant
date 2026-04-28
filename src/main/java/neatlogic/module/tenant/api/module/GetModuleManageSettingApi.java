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
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.dao.mapper.ModuleManageSettingMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = MODULE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetModuleManageSettingApi extends PrivateApiComponentBase {

    @Resource
    private ModuleManageSettingMapper moduleManageSettingMapper;

    @Override
    public String getToken() {
        return "module/manage/setting/get";
    }

    @Override
    public String getName() {
        return "nmtam.getmodulemanagesettingapi.getname";
    }

    @Input({})
    @Output({})
    @Description(desc = "nmtam.getmodulemanagesettingapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        return moduleManageSettingMapper.getModuleManageSetting();
    }
}
