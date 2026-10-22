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
package neatlogic.module.tenant.api.selfcheck;

import neatlogic.framework.selfcheck.annotation.SelfCheckApi;
import org.junit.Assert;
import org.junit.Test;

/** 验证内置自检 API 的默认脚本遵循函数体与严格布尔返回契约。 */
public class SelfCheckDefaultScriptTest {

    /** 四个内置目标均直接返回 reachable 布尔判定，不再声明 verify 函数。 */
    @Test
    public void shouldUseBooleanFunctionBodyAsDefaultScript() {
        assertBooleanFunctionBody(GetSelfCheckProbeApi.class);
        assertBooleanFunctionBody(GetSelfCheckMysqlProbeApi.class);
        assertBooleanFunctionBody(GetSelfCheckMongoDbProbeApi.class);
        assertBooleanFunctionBody(GetSelfCheckElasticsearchProbeApi.class);
    }

    /** 校验单个准入 API 的默认脚本文本。 */
    private void assertBooleanFunctionBody(Class<?> apiClass) {
        SelfCheckApi annotation = apiClass.getAnnotation(SelfCheckApi.class);
        Assert.assertNotNull(annotation);
        Assert.assertEquals("return context.response.reachable === true;", annotation.defaultScript());
        Assert.assertFalse(annotation.defaultScript().contains("function "));
        Assert.assertFalse(annotation.defaultScript().contains("{"));
    }
}
