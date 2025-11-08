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

package neatlogic.module.tenant.api.auditconfig;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auditconfig.dao.mapper.AuditConfigMapper;
import neatlogic.framework.auditconfig.dto.AuditConfigVo;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetAuditConfigApi extends PrivateApiComponentBase {

    @Resource
    private AuditConfigMapper auditConfigMapper;

    @Override
    public String getToken() {
        return "auditconfig/get";
    }

    @Override
    public String getName() {
        return "获取审计配置";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "name", type = ApiParamType.STRING, desc = "名称", isRequired = true)})
    @Output({@Param(explode = AuditConfigVo.class)})
    @Description(desc = "获取审计配置接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        return auditConfigMapper.getAuditConfigByName(jsonObj.getString("name"));
    }

}
