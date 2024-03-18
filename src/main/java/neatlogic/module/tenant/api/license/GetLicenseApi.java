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
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.auth.label.LICENSE_MODIFY;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.dto.LicenseVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.LicenseUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = ADMIN.class)
@AuthAction(action = LICENSE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetLicenseApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "license/get";
    }

    @Override
    public String getName() {
        return "nmtal.getlicenseapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({@Param(explode = LicenseVo.class)})
    @Description(desc = "nmtal.getlicenseapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String licensePk = Config.LICENSE_PK();
        String license = Config.LICENSE();
        if (StringUtils.isNotBlank(license) && StringUtils.isNotBlank(licensePk)) {
            return LicenseUtil.deLicense(license, licensePk);
        }
        return null;
    }
}
