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

package neatlogic.module.tenant.api.mailserver;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NOTIFY_CONFIG_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.util.EmailUtil;
import org.springframework.stereotype.Service;

/**
 * 测试邮件服务器能否正常发送邮件
 *
 * @author linbq
 * @since 2021/5/11 11:21
 **/
@Service
@AuthAction(action = NOTIFY_CONFIG_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class MailServerTestApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "mailserver/test";
    }

    @Override
    public String getName() {
        return "nmtam.mailservertestapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "emailAddress", type = ApiParamType.EMAIL, isRequired = true, desc = "common.mailaddress")
    })
    @Output({})
    @Description(desc = "nmtam.mailservertestapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        EmailUtil.sendEmailWithFile(
                "Test mail",
                "Your configured mail server information is available!",
                jsonObj.getString("emailAddress")
        );
        return null;
    }

}
