/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.tenant.api.featureusageaudit;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.FeatureUsageAuditMapper;
import neatlogic.framework.dao.mapper.LoginMapper;
import neatlogic.framework.dto.featureusageaudit.FeatureUsageAuditVo;
import neatlogic.framework.dto.loginaudit.LoginAuditVo;
import neatlogic.framework.exception.type.ParamInvalidException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.SnowflakeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.CREATE)
//@Transactional
public class SaveFeatureUsageAuditApi extends PrivateApiComponentBase {

    private static final long MAX_DURATION = TimeUnit.DAYS.toMillis(1);

    @Resource
    private FeatureUsageAuditMapper featureUsageAuditMapper;

    @Resource
    private LoginMapper loginMapper;

    @Override
    public String getToken() {
        return "feature/usage/audit/save";
    }

    @Override
    public String getName() {
        return "保存功能使用审计";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "moduleGroup", type = ApiParamType.STRING, isRequired = false, desc = "模块组"),
            @Param(name = "featurePath", type = ApiParamType.STRING, isRequired = true, desc = "menu path"),
            @Param(name = "featureName", type = ApiParamType.STRING, isRequired = true, desc = "menu name"),
            @Param(name = "startTime", type = ApiParamType.LONG, isRequired = true, desc = "start time"),
            @Param(name = "endTime", type = ApiParamType.LONG, isRequired = true, desc = "end time"),
            @Param(name = "duration", type = ApiParamType.LONG, isRequired = true, desc = "duration in milliseconds"),
            @Param(name = "url", type = ApiParamType.STRING, desc = "full url")
    })
    @Output({
            @Param(name = "id", type = ApiParamType.LONG, desc = "audit id")
    })
    @Description(desc = "保存功能使用审计")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        FeatureUsageAuditVo auditVo = JSON.toJavaObject(jsonObj, FeatureUsageAuditVo.class);
        validate(auditVo);
        String userUuid = UserContext.get().getUserUuid();
        auditVo.setUserUuid(userUuid);
        auditVo.setId(SnowflakeUtil.uniqueLong());
        LoginAuditVo lastLoginAudit = loginMapper.getLastLoginAuditByUserUuid(userUuid);
        if (lastLoginAudit != null) {
            auditVo.setLoginAuditId(lastLoginAudit.getId());
        }
        featureUsageAuditMapper.insertFeatureUsageAudit(auditVo);
        JSONObject resultObj = new JSONObject();
        resultObj.put("id", auditVo.getId());
        return resultObj;
    }

    private void validate(FeatureUsageAuditVo auditVo) {
        if (StringUtils.isBlank(auditVo.getModuleGroup())) {
            throw new ParamInvalidException("moduleGroup", auditVo.getModuleGroup());
        }
        if (StringUtils.isBlank(auditVo.getFeaturePath())) {
            throw new ParamInvalidException("featurePath", auditVo.getFeaturePath());
        }
        if (auditVo.getStartTime() == null) {
            throw new ParamInvalidException("startTime", null);
        }
        if (auditVo.getEndTime() == null) {
            throw new ParamInvalidException("endTime", null);
        }
        Long duration = auditVo.getDuration();
        if (duration == null || duration <= 0 || duration > MAX_DURATION) {
            throw new ParamInvalidException("duration", String.valueOf(duration));
        }
        if (auditVo.getEndTime().before(auditVo.getStartTime())) {
            throw new ParamInvalidException("endTime", String.valueOf(auditVo.getEndTime().getTime()));
        }
    }
}
