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

package neatlogic.module.tenant.api.license;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.dto.license.LicenseInvalidVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.LicenseUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetLicenseInvalidMsgApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "license/invalid/msg/get";
    }

    @Override
    public String getName() {
        return "获取许可异常信息";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Description(desc = "获取许可异常信息")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Map<String, LicenseInvalidVo> licenseInvalidVoMap = LicenseUtil.tenantLicenseInvalidTipsMap.get(TenantContext.get().getTenantUuid());
        if (MapUtils.isNotEmpty(licenseInvalidVoMap)) {
            return licenseInvalidVoMap.values();
        }
        return CollectionUtils.EMPTY_COLLECTION;
    }
}
