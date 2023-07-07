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

package neatlogic.module.tenant.api.notify;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.RequestContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.notify.core.INotifyPolicyHandler;
import neatlogic.framework.notify.core.NotifyHandlerType;
import neatlogic.framework.notify.core.NotifyPolicyHandlerFactory;
import neatlogic.framework.notify.dao.mapper.NotifyMapper;
import neatlogic.framework.notify.dto.NotifyPolicyHandlerVo;
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
import java.util.Locale;

@Component
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetNotifyPolicyDefaultTemplateApi extends PrivateApiComponentBase {

    private final String CLASSPATH_ROOT = "classpath:neatlogic/resources/notifypolicytemplate/";

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
        NotifyPolicyHandlerVo notifyPolicyHandlerVo = NotifyPolicyHandlerFactory.getNotifyPolicyHandlerVo(handler);
        if (notifyPolicyHandlerVo == null) {
            throw new NotifyPolicyHandlerNotFoundException(handler);
        }
        String moduleGroup = notifyPolicyHandlerVo.getModuleGroup();
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
        Resource resource = resolver.getResource(CLASSPATH_ROOT + moduleGroup + "/" + simpleHandlerName + "/" + trigger + "/" + notifyHandlerType + "/" + locale + ".html");
        if (!resource.exists()) {
            resource = resolver.getResource(CLASSPATH_ROOT + moduleGroup + "/" + trigger + "/" + notifyHandlerType + "/" + locale + ".html");
            if (!resource.exists()) {
                return null;
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