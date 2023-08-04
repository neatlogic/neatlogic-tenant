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

package neatlogic.module.tenant.api.mailserver;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.auth.label.MAIL_SERVER_MODIFY;
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
@AuthAction(action = MAIL_SERVER_MODIFY.class)
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
