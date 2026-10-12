package neatlogic.module.tenant.api.healthcheck;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.dto.healthcheck.ThreadSnapshotVo;
import neatlogic.framework.exception.type.PermissionDeniedException;
import neatlogic.framework.restful.dto.ApiVo;
import neatlogic.module.tenant.service.healthcheck.ThreadSnapshotService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Collections;

/** 经过真实 API 执行入口验证管理员权限在采集前生效。 */
public class CaptureThreadSnapshotApiTest {
    private final CaptureThreadSnapshotApi api = new CaptureThreadSnapshotApi();
    private final ThreadSnapshotVo snapshot = new ThreadSnapshotVo();
    private Field checkerField;
    private Object originalChecker;
    private int captureCount;

    /** 在测试中注入服务替身，隔离管理员查询依赖。 */
    @Before
    public void setup() throws Exception {
        checkerField = AuthActionChecker.class.getDeclaredField("userMapper");
        checkerField.setAccessible(true);
        originalChecker = checkerField.get(null);
        checkerField.set(null, Proxy.newProxyInstance(UserMapper.class.getClassLoader(),
                new Class<?>[]{UserMapper.class}, (proxy, method, args) -> Collections.emptyList()));
        ThreadSnapshotService service = () -> {
            captureCount++;
            return snapshot;
        };
        Field serviceField = CaptureThreadSnapshotApi.class.getDeclaredField("threadSnapshotService");
        serviceField.setAccessible(true);
        serviceField.set(api, service);
        TenantContext.init("snapshot-test");
    }

    /** 恢复静态权限依赖并释放线程上下文。 */
    @After
    public void cleanup() throws Exception {
        checkerField.set(null, originalChecker);
        if (UserContext.get() != null) {
            UserContext.get().release();
        }
        TenantContext.get().release();
    }

    /** 管理员无需参数即可取得服务返回的完整结构化快照。 */
    @Test
    public void shouldCaptureForAdministrator() throws Exception {
        initUser(true);
        Assert.assertSame(snapshot, execute());
        Assert.assertEquals(1, captureCount);
        Assert.assertEquals("/healthcheck/thread/snapshot", api.getToken());
    }

    /** 普通用户被框架拒绝时不得执行 JVM 采集。 */
    @Test
    public void shouldDenyNonAdministratorBeforeCapture() throws Exception {
        initUser(false);
        try {
            execute();
            Assert.fail("普通用户不能采集线程快照");
        } catch (PermissionDeniedException expected) {
            Assert.assertEquals(0, captureCount);
        }
    }

    /** 建立管理员或普通用户身份。 */
    private void initUser(boolean administrator) {
        UserVo user = new UserVo();
        user.setUuid("snapshot-test-user");
        user.setUserId("snapshot-test-user");
        user.setIsSuperAdmin(administrator);
        user.setAuthorization("test-authorization");
        UserContext.init(user, new AuthenticationInfoVo(user.getUuid()));
    }

    /** 经过私有接口统一执行流程，避免直接调用绕过鉴权。 */
    private Object execute() throws Exception {
        ApiVo apiVo = new ApiVo();
        apiVo.setToken(api.getToken());
        apiVo.setModuleId("tenant");
        apiVo.setNeedAudit(0);
        apiVo.setIsActive(1);
        return api.doService(apiVo, new JSONObject(), null);
    }
}
