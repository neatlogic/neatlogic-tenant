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

package neatlogic.module.tenant.api.license;

import com.alibaba.fastjson.JSONObject;
import neatlogic.delicense.LicenseUtil;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.LICENSE_MODIFY;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@AuthAction(action = LICENSE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
@Service
public class GetLicenseApi extends PrivateApiComponentBase {

    @Override
    public String getName() {
        return "获取license";
    }

    @Override
    public String getToken() {
        return "license/get";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({

    })
    @Output({
    })
    @Description(desc = "获取license接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        if(StringUtils.isNotBlank(Config.LICENSE())) {
            JSONObject result = new JSONObject();
            String licenseStr = LicenseUtil.deLicense(Config.LICENSE(), Config.LICENSE_PK(), Config.DB_URL());
            result = JSONObject.parseObject(licenseStr);
            result.put("issueTime", TimeUtil.convertDateToString(result.getDate("issueTime"), TimeUtil.YYYY_MM_DD));
            result.put("expiredTime", TimeUtil.convertDateToString(result.getDate("expireTime"), TimeUtil.YYYY_MM_DD));
            result.put("stopServiceTime", TimeUtil.addDateByDay(result.getDate("expireTime"), result.getInteger("expiredDay"), TimeUtil.YYYY_MM_DD));
            result.put("license",Config.LICENSE());
            return result;
        }
        return null;
    }
}
