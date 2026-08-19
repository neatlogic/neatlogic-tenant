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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NOTIFY_CONFIG_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.RC4Util;
import neatlogic.framework.dao.mapper.NotifyConfigMapper;
import neatlogic.framework.dto.MailServerVo;
import neatlogic.framework.dto.NotifyConfigVo;
import neatlogic.framework.notify.core.NotifyHandlerType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.RegexUtils;
import neatlogic.framework.util.SnowflakeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
@AuthAction(action = NOTIFY_CONFIG_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class MailServerSaveApi extends PrivateApiComponentBase {

    @Resource
    private NotifyConfigMapper notifyConfigMapper;

    @Override
    public String getToken() {
        return "mailserver/save";
    }

    @Override
    public String getName() {
        return "nmtam.mailserversaveapi.getname";
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
    })
    @Output({})
    @Description(desc = "nmtam.mailserversaveapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        MailServerVo mailServerVo = jsonObj.toJavaObject(MailServerVo.class);
        if (StringUtils.isNotBlank(mailServerVo.getPassword())) {
            mailServerVo.setPassword(RC4Util.encrypt(mailServerVo.getPassword()));
        }
        NotifyConfigVo notifyConfigVo = new NotifyConfigVo();
        if (mailServerVo.getId() != null) {
            notifyConfigVo.setId(mailServerVo.getId());
        } else {
            notifyConfigVo.setId(SnowflakeUtil.uniqueLong());
        }
        notifyConfigVo.setName(mailServerVo.getName());
        notifyConfigVo.setIsActive(0);
        notifyConfigVo.setIsDefault(0);
        notifyConfigVo.setType(NotifyHandlerType.EMAIL.getValue());
        notifyConfigVo.setConfigStr(JSON.toJSONString(mailServerVo));
        notifyConfigMapper.insertNotifyConfigVo(notifyConfigVo);
        return null;
    }

}
