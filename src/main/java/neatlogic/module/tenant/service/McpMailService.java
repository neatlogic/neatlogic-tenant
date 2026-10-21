/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.tenant.service;

import neatlogic.framework.util.$;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.auth.label.NOTIFY_CONFIG_MODIFY;
import neatlogic.framework.dao.mapper.NotifyConfigMapper;
import neatlogic.framework.dto.MailServerVo;
import neatlogic.framework.notify.core.NotifyHandlerType;
import neatlogic.framework.util.EmailUtil;
import neatlogic.module.tenant.exception.McpMailException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/** 通知模块邮件业务服务，复用原权限、服务器配置和邮件发送实现。 */
@Service
public class McpMailService {

    @Resource
    NotifyConfigMapper notifyConfigMapper;

    /** 沿用原通知权限、邮件服务器和发送字段校验。 */
    private String validateParams(JSONObject paramObj) {
        if (!AuthActionChecker.check(ADMIN.class, NOTIFY_CONFIG_MODIFY.class)) {
            return $.t("nmai.aitool.sendemailtool.message.noauth");
        }
        String config = notifyConfigMapper.getConfigByType(NotifyHandlerType.EMAIL.getValue());
        if (StringUtils.isBlank(config)) {
            return $.t("nmai.aitool.sendemailtool.message.nomailserver");
        }

        List<String> list = new ArrayList<>();
        String from = paramObj.getString("from");
        if (StringUtils.isNotBlank(from)) {
            String password = paramObj.getString("password");
            if (StringUtils.isBlank(password)) {
                list.add($.t("nmai.aitool.sendemailtool.message.frompassword", from));
            }
        }
        String subject = paramObj.getString("subject");
        String body = paramObj.getString("body");
        String to = paramObj.getString("to");
        if (StringUtils.isBlank(subject)) {
            list.add($.t("nmai.aitool.sendemailtool.param.subject"));
        }
        if (StringUtils.isBlank(body)) {
            list.add($.t("nmai.aitool.sendemailtool.param.body"));
        }
        if (StringUtils.isBlank(to)) {
            list.add($.t("nmai.aitool.sendemailtool.param.to"));
        }
        if (CollectionUtils.isNotEmpty(list)) {
            return $.t("nmai.aitool.sendemailtool.message.missingparams", String.join($.t("nmai.aitool.sendemailtool.message.paramseparator"), list));
        }
        return null;
    }

    /** 按原规则合并发件身份及收件人，最终委托平台邮件工具发送。 */
    public JSONObject send(JSONObject paramObj) throws Exception {
        String error = validateParams(paramObj);
        if (StringUtils.isNotBlank(error)) {
            throw new McpMailException(error);
        }
        String config = notifyConfigMapper.getConfigByType(NotifyHandlerType.EMAIL.getValue());
        MailServerVo mailServerVo = JSON.parseObject(config, MailServerVo.class);
        String from = paramObj.getString("from");
        if (StringUtils.isNotBlank(from)) {
            String password = paramObj.getString("password");
            mailServerVo.setUserName(from);
            mailServerVo.setPassword(password);
            mailServerVo.setFromAddress(from);
        }
        mailServerVo.setName(UserContext.get().getUserName());
        String subject = paramObj.getString("subject");
        String body = paramObj.getString("body");
        String to = paramObj.getString("to");
        String cc = paramObj.getString("cc");
        Set<String> toEmailSet = new HashSet<>();
        if (StringUtils.isNotBlank(to)) {
            if (to.contains(",")) {
                String[] split = to.split(",");
                for (String str : split) {
                    toEmailSet.add(str.trim());
                }
            } else {
                toEmailSet.add(to);
            }
        }
        Set<String> ccEmailSet = new HashSet<>();
        if (StringUtils.isNotBlank(cc)) {
            if (cc.contains(",")) {
                String[] split = cc.split(",");
                for (String str : split) {
                    ccEmailSet.add(str.trim());
                }
            } else {
                ccEmailSet.add(cc);
            }
        }
        EmailUtil.sendEmailWithFile(
                subject,
                body,
                new ArrayList<>(toEmailSet),
                new ArrayList<>(ccEmailSet),
                null, mailServerVo
        );
        return new JSONObject();
    }

}
