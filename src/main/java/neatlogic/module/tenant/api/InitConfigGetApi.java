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

package neatlogic.module.tenant.api;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.dto.ThemeVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class InitConfigGetApi extends PrivateApiComponentBase {
    private final Logger logger = LoggerFactory.getLogger(InitConfigGetApi.class);
    private final static Set<String> moduleSet = new HashSet<>();
    private boolean isLoad = false;

    @Override
    public String getName() {
        return "获取系统初始化配置";
    }

    @Override
    public String getToken() {
        return "init/config/get";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({})
    @Output({
            @Param(explode = ThemeVo.class)
    })
    @Description(desc = "获取主题配置接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject result = new JSONObject();
        if (!isLoad) {
            getCommercialModule();
        }
        result.put("commercialModuleSet", moduleSet);
        result.put("ssoTicketKey", Config.SSO_TICKET_KEY());
        return result;
    }

    private void getCommercialModule() {
        Reflections reflections = new Reflections("neatlogic");
        Set<Class<? extends InstantiationAwareBeanPostProcessor>> authClass = reflections.getSubTypesOf(InstantiationAwareBeanPostProcessor.class);
        for (Class<? extends InstantiationAwareBeanPostProcessor> c : authClass) {
            try {
                if (!c.getSimpleName().endsWith("AuthBean")) {
                    continue;
                }
                Field field = c.getDeclaredField("moduleSet");
                field.setAccessible(true);
                Object value = field.get(null);
                moduleSet.addAll((Collection<? extends String>) value);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
        isLoad = true;

    }
}
