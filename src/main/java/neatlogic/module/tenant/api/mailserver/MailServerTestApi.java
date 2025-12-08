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

package neatlogic.module.tenant.api.mailserver;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NOTIFY_CONFIG_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.MailServerVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.EmailUtil;
import neatlogic.framework.util.RegexUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 测试邮件服务器能否正常发送邮件
 *
 * @author linbq
 * @since 2021/5/11 11:21
 **/
@Service
@AuthAction(action = NOTIFY_CONFIG_MODIFY.class)
@OperationType(type = OperationTypeEnum.OPERATE)
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
            @Param(name = "id", type = ApiParamType.STRING, desc = "common.id"),
            @Param(name = "name", type = ApiParamType.REGEX, rule = RegexUtils.NAME, isRequired = true, maxLength = 50, desc = "common.name"),
            @Param(name = "host", type = ApiParamType.STRING, isRequired = true, maxLength = 50, desc = "term.framework.smpthost"),
            @Param(name = "port", type = ApiParamType.INTEGER, isRequired = true, desc = "term.framework.smptport"),
            @Param(name = "userName", type = ApiParamType.STRING, maxLength = 50, desc = "common.username"),
            @Param(name = "password", type = ApiParamType.STRING, maxLength = 50, desc = "common.password"),
            @Param(name = "homeUrl", type = ApiParamType.STRING, desc = "common.homeurl"),
            @Param(name = "fromAddress", type = ApiParamType.STRING, isRequired = true, maxLength = 50, desc = "common.mailaddress"),
            @Param(name = "sslEnable", type = ApiParamType.ENUM, rule = "true,false", isRequired = true, maxLength = 50, desc = "term.framework.smptsslenable"),
            @Param(name = "emailAddress", type = ApiParamType.EMAIL, isRequired = true, desc = "common.mailaddress")
    })
    @Output({})
    @Description(desc = "nmtam.mailservertestapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
//        EmailUtil.sendHtmlEmail(
//                "Test mail",
//                "Your configured mail server information is available!",
//                Collections.singletonList(jsonObj.getString("emailAddress")),
//                null
//        );
        MailServerVo mailServerVo = jsonObj.toJavaObject(MailServerVo.class);
        EmailUtil.sendEmailWithFile(
                "Test mail",
                "Your configured mail server information is available!",
                Collections.singletonList(jsonObj.getString("emailAddress")),
                null, null, mailServerVo);
        return null;
    }

}
