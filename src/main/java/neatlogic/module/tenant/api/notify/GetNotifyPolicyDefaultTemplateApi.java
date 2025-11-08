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

package neatlogic.module.tenant.api.notify;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.RequestContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.notify.core.INotifyPolicyHandler;
import neatlogic.framework.notify.core.NotifyHandlerType;
import neatlogic.framework.notify.core.NotifyPolicyHandlerFactory;
import neatlogic.framework.notify.dao.mapper.NotifyMapper;
import neatlogic.framework.notify.dto.NotifyPolicyVo;
import neatlogic.framework.notify.dto.NotifyTriggerTemplateVo;
import neatlogic.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import neatlogic.framework.notify.exception.NotifyPolicyNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Locale;

@Component
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetNotifyPolicyDefaultTemplateApi extends PrivateApiComponentBase {

    private final String CLASSPATH_ROOT = "classpath:neatlogic/resources/{0}/notifypolicytemplate/";

    @Autowired
    private NotifyMapper notifyMapper;

    @Override
    public String getName() {
        return "nmtan.getnotifypolicydefaulttemplateapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "policyId", type = ApiParamType.LONG, isRequired = true, desc = "term.framework.policyid"),
            @Param(name = "trigger", type = ApiParamType.STRING, isRequired = true, minLength = 1, desc = "common.trigger"),
            @Param(name = "notifyHandlerType", type = ApiParamType.ENUM, member = NotifyHandlerType.class, desc = "common.notifytype")
    })
    @Output({
           @Param(explode = NotifyTriggerTemplateVo.class,desc = "term.framework.notifypolicydefaulttemplateinfo")
    })
    @Description(desc = "nmtan.getnotifypolicydefaulttemplateapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long policyId = paramObj.getLong("policyId");
        NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
        if (notifyPolicyVo == null) {
            throw new NotifyPolicyNotFoundException(policyId.toString());
        }
        String handler = notifyPolicyVo.getHandler();
        INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(handler);
        if (notifyPolicyHandler == null) {
            throw new NotifyPolicyHandlerNotFoundException(handler);
        }
        String moduleGroup = NotifyPolicyHandlerFactory.getModuleGroupIdByHandler(handler);
        if (moduleGroup == null) {
            throw new NotifyPolicyHandlerNotFoundException(handler);
        }
        String module = NotifyPolicyHandlerFactory.getModuleIdByHandler(handler);
        if (module == null) {
            throw new NotifyPolicyHandlerNotFoundException(handler);
        }
        int index = handler.lastIndexOf('.');
        String simpleHandlerName = handler.substring(index + 1);
        simpleHandlerName = simpleHandlerName.toLowerCase();
        String trigger = paramObj.getString("trigger");
        String notifyHandlerType = paramObj.getString("notifyHandlerType");
        if (StringUtils.isBlank(notifyHandlerType)) {
            notifyHandlerType = NotifyHandlerType.EMAIL.getValue();
        }
        Locale locale = RequestContext.get() != null ? RequestContext.get().getLocale() : Locale.getDefault();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        // 先到处理器所在模块查找默认模板
        String classpathRoot = MessageFormat.format(CLASSPATH_ROOT, module);
        Resource resource = resolver.getResource(classpathRoot + "notifypolicyhandler/" + simpleHandlerName + "/" + trigger + "/" + notifyHandlerType + "/" + locale + ".html");
        if (!resource.exists()) {
            resource = resolver.getResource(classpathRoot + "trigger/" + trigger + "/" + notifyHandlerType + "/" + locale + ".html");
            if (!resource.exists()) {
                // 再到处理器所在模块组查找默认模板
                classpathRoot = MessageFormat.format(CLASSPATH_ROOT, moduleGroup);
                resource = resolver.getResource(classpathRoot + "trigger/" + trigger + "/" + notifyHandlerType + "/" + locale + ".html");
                if (!resource.exists()) {
                    return null;
                }
            }
        }
        StringWriter writer = new StringWriter();
        IOUtils.copy(resource.getInputStream(), writer, StandardCharsets.UTF_8);
        String html = writer.toString();
        writer.close();
        String title = StringUtils.EMPTY;
        String content = StringUtils.EMPTY;
        Document document = Jsoup.parse(html);
        Elements htmlElements = document.getElementsByTag("html");
        if (CollectionUtils.isNotEmpty(htmlElements)) {
            Elements headElements = htmlElements.get(0).getElementsByTag("head");
            if (CollectionUtils.isNotEmpty(headElements)) {
                Elements titleElements = headElements.get(0).getElementsByTag("title");
                if (CollectionUtils.isNotEmpty(titleElements)) {
                    title = titleElements.get(0).html();
                }
            }
            Elements bodyElements = htmlElements.get(0).getElementsByTag("body");
            if (CollectionUtils.isNotEmpty(bodyElements)) {
                content = bodyElements.get(0).html();
            }
        }
        NotifyTriggerTemplateVo notifyTriggerTemplateVo = new NotifyTriggerTemplateVo();
        notifyTriggerTemplateVo.setTrigger(trigger);
        notifyTriggerTemplateVo.setTitle(title);
        notifyTriggerTemplateVo.setContent(content);
        return notifyTriggerTemplateVo;
    }

    @Override
    public String getToken() {
        return "notify/policy/default/template/get";
    }
}
