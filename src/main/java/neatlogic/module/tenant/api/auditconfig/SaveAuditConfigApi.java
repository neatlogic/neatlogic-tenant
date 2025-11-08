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
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
@AuthAction(action = ADMIN.class)
public class SaveAuditConfigApi extends PrivateApiComponentBase {

    @Resource
    private AuditConfigMapper auditConfigMapper;

    @Override
    public String getToken() {
        return "auditconfig/save";
    }

    @Override
    public String getName() {
        return "保存审计配置";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "name", type = ApiParamType.STRING, desc = "名称", isRequired = true),
            @Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "配置")})
    @Description(desc = "保存审计配置接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        AuditConfigVo auditConfigVo = JSONObject.toJavaObject(jsonObj, AuditConfigVo.class);
        if (MapUtils.isNotEmpty(auditConfigVo.getConfig())) {
            auditConfigMapper.saveAuditConfig(auditConfigVo);
        } else {
            auditConfigMapper.deleteAuditConfig(auditConfigVo.getName());
        }
        return null;
    }

}
