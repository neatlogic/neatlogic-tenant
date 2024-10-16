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

package neatlogic.module.tenant.api.license;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.LicenseUtil;
import org.springframework.stereotype.Service;

@Service
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
        return LicenseUtil.licenseInvalidTipsMap.values();
    }
}
