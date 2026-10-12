package neatlogic.module.tenant.service.healthcheck;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.dto.healthcheck.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/** 验证诊断证据、等待分类和共享线程池对象的快照隔离。 */
public class ThreadSnapshotServiceTest {
    /** Quartz、线程池与 Hikari 定时维护等待只展示状态，不产生故障诊断。 */
    @Test
    public void shouldRecognizeIdleAndScheduledWorkersWithoutFaults() {
        Fixture service = new Fixture();
        service.add(thread("1", "TIMED_WAITING", "org.quartz.simpl.SimpleThreadPool$WorkerThread", "run"));
        service.add(thread("2", "WAITING", "java.util.concurrent.ThreadPoolExecutor", "getTask"));
        ThreadSnapshotThreadVo housekeeper = thread("3", "TIMED_WAITING",
                "java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue", "take");
        housekeeper.setName("HikariCP housekeeper");
        housekeeper.getStackTrace().add(frame("java.util.concurrent.ThreadPoolExecutor", "getTask"));
        service.add(housekeeper);
        ThreadSnapshotVo snapshot = service.capture();
        Assert.assertEquals("taskWait", snapshot.getThreads().get(0).getWaitType());
        Assert.assertEquals("taskWait", snapshot.getThreads().get(1).getWaitType());
        Assert.assertEquals("timerWait", housekeeper.getWaitType());
        Assert.assertEquals(housekeeper.getStackTrace().get(0).getText(), housekeeper.getWaitEvidence());
        Assert.assertTrue(snapshot.getDiagnostics().isEmpty());
    }

    /** 连接池等待优先于通用工作线程等待，网络读取可以是 RUNNABLE。 */
    @Test
    public void shouldIdentifyConnectionAndNetworkEvidence() {
        Fixture service = new Fixture();
        ThreadSnapshotThreadVo connection = thread("1", "WAITING", "com.zaxxer.hikari.util.ConcurrentBag", "borrow");
        connection.getStackTrace().add(frame("java.util.concurrent.ThreadPoolExecutor", "getTask"));
        service.add(connection);
        service.add(thread("2", "RUNNABLE", "sun.nio.ch.NioSocketImpl", "implRead"));
        service.capture();
        Assert.assertEquals("connectionWait", connection.getWaitType());
        Assert.assertEquals(connection.getStackTrace().get(0).getText(), connection.getWaitEvidence());
        Assert.assertEquals("networkRead", service.snapshot.getThreads().get(1).getWaitType());
        Assert.assertTrue(service.snapshot.getDiagnostics().isEmpty());
    }

    /** 线程名称和相似类名不触发分类，非等待状态不被标为空闲等待。 */
    @Test
    public void shouldNotInferWaitFromNamesOrUnrelatedFrames() {
        Fixture service = new Fixture();
        ThreadSnapshotThreadVo nameOnly = thread("1", "WAITING", "example.HikariPool", "getConnection");
        nameOnly.setName("HikariCP housekeeper Quartz worker");
        service.add(nameOnly);
        service.add(thread("2", "RUNNABLE", "java.util.concurrent.ThreadPoolExecutor", "getTask"));
        service.add(thread("3", "BLOCKED", "com.zaxxer.hikari.pool.HikariPool", "getConnection"));
        service.capture();
        for (ThreadSnapshotThreadVo thread : service.snapshot.getThreads()) {
            Assert.assertEquals("other", thread.getWaitType());
            Assert.assertNull(thread.getWaitEvidence());
        }
        Assert.assertTrue(service.snapshot.getDiagnostics().isEmpty());
    }

    /** 业务任务按线程 ID 关联，采集时长与日期复制后不随共享对象变动。 */
    @Test
    public void shouldCopyAndFreezeTaskWithoutMutatingLivePool() {
        Fixture service = new Fixture();
        service.add(thread("17", "RUNNABLE", "neatlogic.module.test.Runner", "execute"));
        ThreadTaskVo liveTask = new ThreadTaskVo();
        liveTask.setId(17L);
        liveTask.setName("captured task");
        liveTask.setTenantUuid("tenant-a");
        liveTask.setStartTime(new Date(3_000L));
        service.pool.setThreadTaskList(new ArrayList<>(Collections.singletonList(liveTask)));
        ThreadVo liveThread = new ThreadVo(17L, "pool-worker");
        liveThread.setStartTime(new Date(2_000L));
        service.pool.setThreadList(new ArrayList<>(Collections.singletonList(liveThread)));
        ThreadSnapshotVo snapshot = service.capture();
        ThreadTaskVo capturedTask = snapshot.getThreads().get(0).getTask();
        Assert.assertNotSame(liveTask, capturedTask);
        Assert.assertSame(capturedTask, snapshot.getThreadPool().getThreadTaskList().get(0));
        Assert.assertEquals(7_000L, capturedTask.getTimeCost());
        Assert.assertEquals("tenant-a", snapshot.getCurrentTenantUuid());
        Assert.assertEquals("neatlogic.module.test.Runner.execute(Test.java:1)",
                snapshot.getThreads().get(0).getFirstBusinessFrame());
        liveTask.setName("changed");
        liveTask.getStartTime().setTime(9_000L);
        liveThread.getStartTime().setTime(9_000L);
        Assert.assertEquals("captured task", capturedTask.getName());
        Assert.assertEquals(3_000L, capturedTask.getStartTime().getTime());
        Assert.assertEquals(8_000L, snapshot.getThreadPool().getThreadList().get(0).getTimeCost());
        // 验证真实 JSON 序列化也读取固定 getter，导出不重新计算当前时长。
        JSONObject json = JSON.parseObject(JSON.toJSONString(snapshot));
        Assert.assertEquals(7_000L, json.getJSONArray("threads").getJSONObject(0).getJSONObject("task").getLongValue("timeCost"));
        Assert.assertEquals(8_000L, json.getJSONObject("threadPool").getJSONArray("threadList").getJSONObject(0).getLongValue("timeCost"));
    }

    /** 全 JVM 线程保留，无法关联任务的线程不会被归为当前租户。 */
    @Test
    public void shouldRetainUnassociatedThreadsAndMissingCpuTime() {
        Fixture service = new Fixture();
        service.add(thread("1", "WAITING", "java.lang.Object", "wait"));
        service.capture();
        Assert.assertEquals(1, service.snapshot.getThreads().size());
        Assert.assertNull(service.snapshot.getThreads().get(0).getTask());
        Assert.assertNull(service.snapshot.getThreads().get(0).getCpuTimeMs());
        Assert.assertTrue(service.snapshot.getDiagnostics().isEmpty());
    }

    /** 相同持有者的不同锁分别聚合，缺失持有者的阻塞不编造锁关系。 */
    @Test
    public void shouldGroupBlockedThreadsByOwnerAndLock() {
        Fixture service = new Fixture();
        service.add(blocked("1", "90", "lock-A"));
        service.add(blocked("2", "90", "lock-A"));
        service.add(blocked("3", "90", "lock-B"));
        service.add(blocked("4", null, "lock-A"));
        List<ThreadSnapshotDiagnosticVo> diagnostics = service.capture().getDiagnostics();
        Assert.assertEquals(2, diagnostics.size());
        Assert.assertEquals("lockContention", diagnostics.get(0).getCode());
        Assert.assertEquals("informational", diagnostics.get(0).getSeverity());
        Assert.assertEquals("90", diagnostics.get(0).getOwnerId());
        Assert.assertEquals("lock-A", diagnostics.get(0).getLockName());
        Assert.assertEquals(Arrays.asList("1", "2"), diagnostics.get(0).getThreadIds());
        Assert.assertTrue(diagnostics.get(0).getEvidence().get(0).contains("owner=90"));
        Assert.assertEquals(Collections.singletonList("3"), diagnostics.get(1).getThreadIds());
    }

    /** 同步器竞争处于 WAITING 时保留持有者关系，普通条件等待不编造关系。 */
    @Test
    public void shouldIncludeSynchronizerWaitWithKnownOwner() {
        Fixture service = new Fixture();
        ThreadSnapshotThreadVo synchronizer = blocked("1", "90", "java.util.concurrent.locks.ReentrantLock$NonfairSync@a");
        synchronizer.setState("WAITING");
        service.add(synchronizer);
        ThreadSnapshotThreadVo timedSynchronizer = blocked("2", "90", synchronizer.getLockName());
        timedSynchronizer.setState("TIMED_WAITING");
        service.add(timedSynchronizer);
        ThreadSnapshotThreadVo condition = blocked("3", null, "condition@b");
        condition.setState("WAITING");
        service.add(condition);
        service.add(blocked("4", "-1", "lock-C"));
        List<ThreadSnapshotDiagnosticVo> diagnostics = service.capture().getDiagnostics();
        Assert.assertEquals(1, diagnostics.size());
        Assert.assertEquals("informational", diagnostics.get(0).getSeverity());
        Assert.assertEquals(Arrays.asList("1", "2"), diagnostics.get(0).getThreadIds());
        Assert.assertEquals("90", diagnostics.get(0).getOwnerId());
    }

    /** 只有 JVM 明确返回的死锁线程才生成确认问题。 */
    @Test
    public void shouldUseJvmDeadlockResultAsConfirmedProblem() {
        Fixture service = new Fixture();
        service.add(blocked("1", "2", "lock-B"));
        service.add(blocked("2", "1", "lock-A"));
        service.snapshot.setDeadlockedThreadIds(Arrays.asList("1", "2"));
        ThreadSnapshotDiagnosticVo diagnostic = service.capture().getDiagnostics().get(0);
        Assert.assertEquals("deadlock", diagnostic.getCode());
        Assert.assertEquals("confirmed", diagnostic.getSeverity());
        Assert.assertEquals(Arrays.asList("1", "2"), diagnostic.getThreadIds());
        Assert.assertEquals(2, diagnostic.getEvidence().size());
        Assert.assertTrue(diagnostic.getEvidence().get(0).contains("lock=lock-B"));
    }

    /** 容量大于零且已经满载并排队才提示压力，单独排队或满载不报警。 */
    @Test
    public void shouldRequireCapacitySaturationAndQueueForPressure() {
        Fixture service = new Fixture();
        service.pool.setMaxThreadCount(3);
        service.pool.setMainActiveCount(2);
        service.pool.setMainQueueSize(1);
        Assert.assertTrue(service.capture().getDiagnostics().isEmpty());
        service.pool.setMainActiveCount(3);
        service.pool.setMainQueueSize(0);
        Assert.assertTrue(service.capture().getDiagnostics().isEmpty());
        service.pool.setMainQueueSize(2);
        ThreadSnapshotDiagnosticVo pressure = service.capture().getDiagnostics().get(0);
        Assert.assertEquals("poolPressure", pressure.getCode());
        Assert.assertEquals("suspect", pressure.getSeverity());
        Assert.assertEquals(Arrays.asList("active=3", "max=3", "queued=2"), pressure.getEvidence());
        service.pool.setMaxThreadCount(0);
        Assert.assertTrue(service.capture().getDiagnostics().isEmpty());
    }

    /** 创建带明确匹配帧的线程样本。 */
    private ThreadSnapshotThreadVo thread(String id, String state, String clazz, String method) {
        ThreadSnapshotThreadVo thread = new ThreadSnapshotThreadVo();
        thread.setId(id);
        thread.setName("thread-" + id);
        thread.setState(state);
        thread.getStackTrace().add(frame(clazz, method));
        return thread;
    }

    /** 创建带持有者关系的阻塞线程。 */
    private ThreadSnapshotThreadVo blocked(String id, String owner, String lock) {
        ThreadSnapshotThreadVo thread = thread(id, "BLOCKED", "neatlogic.module.test.Runner", "execute");
        thread.setLockOwnerId(owner);
        thread.setLockName(lock);
        return thread;
    }

    /** 建立结构化帧及可展示证据文本。 */
    private ThreadSnapshotFrameVo frame(String clazz, String method) {
        ThreadSnapshotFrameVo frame = new ThreadSnapshotFrameVo();
        frame.setClassName(clazz);
        frame.setMethodName(method);
        frame.setText(clazz + "." + method + "(Test.java:1)");
        return frame;
    }

    /** 替换外部采集边界，保留真实业务诊断和任务复制逻辑。 */
    private static class Fixture extends ThreadSnapshotServiceImpl {
        private final ThreadSnapshotVo snapshot = new ThreadSnapshotVo();
        private final ThreadPoolVo pool = new ThreadPoolVo();

        /** 固定采样时刻，避免测试依赖墙钟时间。 */
        private Fixture() {
            snapshot.setCapturedAt(10_000L);
        }

        /** 加入待分析线程。 */
        private void add(ThreadSnapshotThreadVo thread) {
            snapshot.getThreads().add(thread);
        }

        /** 返回测试快照，不启动真实线程。 */
        @Override
        protected ThreadSnapshotVo collectThreads() {
            return snapshot;
        }

        /** 返回测试线程池，验证诊断和复制对原对象的隔离。 */
        @Override
        protected ThreadPoolVo collectThreadPool() {
            return pool;
        }

        /** 固定当前租户上下文。 */
        @Override
        protected String currentTenantUuid() {
            return "tenant-a";
        }
    }
}
