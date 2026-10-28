/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the Sustainable Use License (SUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.service.selfcheck;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.util.ModuleUtil;
import neatlogic.framework.dto.module.ModuleVo;
import neatlogic.framework.restful.core.IApiComponent;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentFactory;
import neatlogic.framework.restful.dto.ApiVo;
import neatlogic.framework.selfcheck.annotation.SelfCheckApi;
import neatlogic.framework.selfcheck.dto.SelfCheckScriptResultVo;
import neatlogic.framework.selfcheck.service.SelfCheckScriptService;
import neatlogic.module.tenant.dao.mapper.SelfCheckMapper;
import neatlogic.module.tenant.dto.selfcheck.SelfCheckRunItemVo;
import neatlogic.module.tenant.dto.selfcheck.SelfCheckRunVo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** 验证未保存检查项经真实调试链路生成的入库快照排序，隔离数据库与目标调用。 */
public class SelfCheckDebugItemSortTest {
    private static final String TEST_TENANT = "selfcheck_debug_sort_test";
    private static final String TEST_MODULE = "selfcheck_debug_sort_test_module";
    private SelfCheckServiceImpl service;
    private SelfCheckRunItemVo insertedItem;
    private ApiVo apiVo;
    private ModuleVo moduleVo;
    private IApiComponent previousComponent;
    private Map<String, List<String>> tenantModuleCache;
    private List<String> previousTenantModules;
    private ThreadLocal<TenantContext> tenantContextHolder;
    private ThreadLocal<UserContext> userContextHolder;
    private TenantContext previousTenantContext;
    private UserContext previousUserContext;
    private Map<String, String> previousMdc;

    /** 安装独立租户、准入目标与持久化替身，入库检查项时立即停止以避免实际执行。 */
    @Before
    @SuppressWarnings("unchecked")
    public void setup() throws Exception {
        tenantContextHolder = (ThreadLocal<TenantContext>) field(TenantContext.class, "instance").get(null);
        userContextHolder = (ThreadLocal<UserContext>) field(UserContext.class, "instance").get(null);
        previousTenantContext = tenantContextHolder.get();
        previousUserContext = userContextHolder.get();
        previousMdc = MDC.getCopyOfContextMap();
        tenantModuleCache = (Map<String, List<String>>) field(TenantContext.class, "tenantModuleGroupListMap").get(null);
        previousTenantModules = tenantModuleCache.put(TEST_TENANT, new ArrayList<>(Collections.singletonList(TEST_MODULE)));
        TenantContext.init(TEST_TENANT);
        // 直接构造测试上下文，避免默认初始化触发租户时区等外部配置读取。
        Constructor<UserContext> constructor = UserContext.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        UserContext userContext = constructor.newInstance();
        userContext.setUserUuid("debug-sort-test-user");
        userContextHolder.set(userContext);

        Assert.assertNull(ModuleUtil.getModuleById(TEST_MODULE));
        moduleVo = new ModuleVo();
        moduleVo.setId(TEST_MODULE);
        moduleVo.setGroup(TEST_MODULE);
        ModuleUtil.addModule(moduleVo);
        DebugTargetApi target = new DebugTargetApi();
        apiVo = new ApiVo();
        apiVo.setToken(target.getToken());
        apiVo.setName(target.getName());
        apiVo.setHandlerName(target.getName());
        apiVo.setHandler(DebugTargetApi.class.getName());
        apiVo.setModuleId(TEST_MODULE);
        PrivateApiComponentFactory.getApiList().add(apiVo);
        previousComponent = PrivateApiComponentFactory.getComponentMap().put(apiVo.getHandler(), target);

        SelfCheckMapper mapper = (SelfCheckMapper) Proxy.newProxyInstance(SelfCheckMapper.class.getClassLoader(),
                new Class<?>[]{SelfCheckMapper.class}, (proxy, method, args) -> {
                    if ("insertRun".equals(method.getName())) {
                        ((SelfCheckRunVo) args[0]).setId(101L);
                        return 1;
                    }
                    if ("insertRunItem".equals(method.getName())) {
                        insertedItem = (SelfCheckRunItemVo) args[0];
                        throw new StopBeforeTargetExecution();
                    }
                    throw new AssertionError("不应调用其他持久化方法：" + method.getName());
                });
        SelfCheckScriptService scriptService = (SelfCheckScriptService) Proxy.newProxyInstance(
                SelfCheckScriptService.class.getClassLoader(), new Class<?>[]{SelfCheckScriptService.class},
                (proxy, method, args) -> {
                    if ("validate".equals(method.getName())) {
                        SelfCheckScriptResultVo result = new SelfCheckScriptResultVo();
                        result.setValid(true);
                        return result;
                    }
                    throw new AssertionError("测试不应执行验证脚本");
                });
        service = new SelfCheckServiceImpl();
        field(SelfCheckServiceImpl.class, "selfCheckMapper").set(service, mapper);
        field(SelfCheckServiceImpl.class, "selfCheckScriptService").set(service, scriptService);
    }

    /** 完整恢复注册表、租户缓存、线程上下文与日志上下文，避免污染同进程其他测试。 */
    @After
    public void teardown() {
        if (apiVo != null) {
            PrivateApiComponentFactory.getApiList().remove(apiVo);
            if (previousComponent == null) {
                PrivateApiComponentFactory.getComponentMap().remove(apiVo.getHandler());
            } else {
                PrivateApiComponentFactory.getComponentMap().put(apiVo.getHandler(), previousComponent);
            }
        }
        if (moduleVo != null) {
            ModuleUtil.removeModule(moduleVo);
        }
        if (tenantModuleCache != null) {
            if (previousTenantModules == null) {
                tenantModuleCache.remove(TEST_TENANT);
            } else {
                tenantModuleCache.put(TEST_TENANT, previousTenantModules);
            }
        }
        restoreContext(tenantContextHolder, previousTenantContext);
        restoreContext(userContextHolder, previousUserContext);
        MDC.clear();
        if (previousMdc != null) {
            MDC.setContextMap(previousMdc);
        }
    }

    /** 新建检查项未传排序时，执行快照必须按第一项入库。 */
    @Test
    public void shouldDefaultMissingSortBeforeInsertingDebugItem() {
        assertInsertedSort(debugParams(), 1);
    }

    /** 客户端显式传入空排序时，行为与缺省字段保持一致。 */
    @Test
    public void shouldDefaultExplicitNullSortBeforeInsertingDebugItem() {
        JSONObject params = debugParams();
        params.put("sort", null);
        assertInsertedSort(params, 1);
    }

    /** 只为空排序补值，不覆盖已存在的零值或正整数。 */
    @Test
    public void shouldPreserveExistingDebugSort() {
        for (int sort : new int[]{0, 7}) {
            JSONObject params = debugParams();
            params.put("sort", sort);
            assertInsertedSort(params, sort);
        }
    }

    /** 在真实 Mapper 边界验证排序及快照内容，并确认尚未调用目标接口。 */
    private void assertInsertedSort(JSONObject params, int expectedSort) {
        insertedItem = null;
        Assert.assertThrows(StopBeforeTargetExecution.class, () -> service.testItem(params));
        Assert.assertNotNull(insertedItem);
        Assert.assertEquals(Integer.valueOf(expectedSort), insertedItem.getSort());
        Assert.assertEquals(Long.valueOf(101L), insertedItem.getRunId());
        Assert.assertNull(insertedItem.getScenarioItemId());
        Assert.assertEquals("调试排序检查", insertedItem.getItemName());
        Assert.assertEquals(DebugTargetApi.TOKEN, insertedItem.getApiToken());
        Assert.assertEquals("return false;", insertedItem.getVerifyScript());
        Assert.assertEquals(1, JSON.parseObject(insertedItem.getRequestSnapshot()).getIntValue("pageSize"));
        Assert.assertEquals(1, params.getJSONObject("requestParam").getIntValue("pageSize"));
    }

    /** 构造与新建检查项调试相同的最小请求。 */
    private JSONObject debugParams() {
        JSONObject params = new JSONObject();
        params.put("name", "调试排序检查");
        params.put("apiToken", DebugTargetApi.TOKEN);
        params.put("verifyScript", "return false;");
        params.put("requestParam", JSON.parseObject("{\"pageSize\":1}"));
        return params;
    }

    /** 仅测试通过反射访问既有字段，不扩展生产装配接口。 */
    private static Field field(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    /** 恢复原线程状态，原本为空时移除测试状态。 */
    private static <T> void restoreContext(ThreadLocal<T> holder, T original) {
        if (holder != null) {
            if (original == null) {
                holder.remove();
            } else {
                holder.set(original);
            }
        }
    }

    /** 标记已到达执行快照入库点，随后无需执行目标或脚本。 */
    private static class StopBeforeTargetExecution extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    /** 只提供准入元数据，任何真实调用均令测试失败。 */
    @SelfCheckApi
    public static class DebugTargetApi extends PrivateApiComponentBase {
        private static final String TOKEN = "selfcheck/test/debug-sort-target";

        /** 返回独立测试目标标识。 */
        @Override
        public String getToken() { return TOKEN; }

        /** 返回无需语言资源加载的测试目标名称。 */
        @Override
        public String getName() { return "调试排序测试目标"; }

        /** 快照入库前后均不应实际执行目标查询。 */
        @Override
        public Object myDoService(JSONObject params) {
            throw new AssertionError("测试不应执行真实目标接口");
        }
    }
}
