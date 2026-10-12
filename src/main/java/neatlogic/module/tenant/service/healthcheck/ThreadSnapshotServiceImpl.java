package neatlogic.module.tenant.service.healthcheck;

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadpool.CachedThreadPool;
import neatlogic.framework.dto.healthcheck.*;
import neatlogic.framework.util.ThreadUtil;
import org.springframework.stereotype.Service;

import java.util.*;

/** 将 JVM 快照与采集时业务任务关联，等待状态本身不代表故障。 */
@Service
public class ThreadSnapshotServiceImpl implements ThreadSnapshotService {
    /** 复制共享任务后执行诊断，后续序列化不再读取实时任务状态。 */
    @Override
    public ThreadSnapshotVo capture() {
        long started = System.nanoTime();
        ThreadSnapshotVo snapshot = collectThreads();
        ThreadPoolVo pool = copyPool(collectThreadPool(), snapshot.getCapturedAt());
        snapshot.setThreadPool(pool);
        snapshot.setCurrentTenantUuid(currentTenantUuid());
        Map<String, ThreadTaskVo> tasks = new LinkedHashMap<>();
        for (ThreadTaskVo task : pool.getThreadTaskList()) {
            if (task.getId() != null) {
                tasks.put(String.valueOf(task.getId()), task);
            }
        }
        for (ThreadSnapshotThreadVo thread : snapshot.getThreads()) {
            thread.setTask(tasks.get(thread.getId()));
            describeThread(thread);
        }
        snapshot.setDiagnostics(diagnose(snapshot));
        snapshot.setCaptureDurationMs((System.nanoTime() - started) / 1_000_000L);
        return snapshot;
    }

    /** 隔离实际 JVM 采集，测试可覆盖此边界而不改变生产装配方式。 */
    protected ThreadSnapshotVo collectThreads() {
        return ThreadUtil.captureThreadSnapshot();
    }

    /** 在 JVM 采集后读取一次线程池状态，不调用带租户过滤的业务 API。 */
    protected ThreadPoolVo collectThreadPool() {
        return CachedThreadPool.getStatus();
    }

    /** 只标识当前租户，不为 JVM 公共线程推断租户归属。 */
    protected String currentTenantUuid() {
        if (TenantContext.get() != null) {
            return TenantContext.get().getTenantUuid();
        }
        return null;
    }

    /** 复制线程池指标与任务，避免修改线程池内部共享对象。 */
    private ThreadPoolVo copyPool(ThreadPoolVo source, long capturedAt) {
        ThreadPoolVo copy = new ThreadPoolVo();
        copy.setMainPoolSize(source.getMainPoolSize());
        copy.setMainActiveCount(source.getMainActiveCount());
        copy.setMainQueueSize(source.getMainQueueSize());
        copy.setBackupPoolSize(source.getBackupPoolSize());
        copy.setBackupActiveCount(source.getBackupActiveCount());
        copy.setBackupQueueSize(source.getBackupQueueSize());
        copy.setMaxThreadCount(source.getMaxThreadCount());
        List<ThreadTaskVo> tasks = new ArrayList<>();
        for (ThreadTaskVo sourceTask : source.getThreadTaskList()) {
            ThreadSnapshotTaskVo task = new ThreadSnapshotTaskVo();
            task.setId(sourceTask.getId());
            task.setName(sourceTask.getName());
            task.setPoolName(sourceTask.getPoolName());
            task.setStatus(sourceTask.getStatus());
            task.setPriority(sourceTask.getPriority());
            task.setTenantUuid(sourceTask.getTenantUuid());
            if (sourceTask.getStartTime() != null) {
                task.setStartTime(new Date(sourceTask.getStartTime().getTime()));
                task.setTimeCost(Math.max(0L, capturedAt - task.getStartTime().getTime()));
            }
            tasks.add(task);
        }
        copy.setThreadTaskList(tasks);
        List<ThreadVo> threads = new ArrayList<>();
        for (ThreadVo sourceThread : source.getThreadList()) {
            threads.add(new FrozenPoolThreadVo(sourceThread, capturedAt));
        }
        copy.setThreadList(threads);
        return copy;
    }

    /** 先识别有明确业务含义的等待，再识别正常定时与空闲任务等待。 */
    private void describeThread(ThreadSnapshotThreadVo thread) {
        thread.setWaitType("other");
        for (ThreadSnapshotFrameVo frame : thread.getStackTrace()) {
            if (frame.getClassName() != null && frame.getClassName().startsWith("neatlogic.")) {
                thread.setFirstBusinessFrame(frame.getText());
                break;
            }
        }
        boolean waiting = Objects.equals("WAITING", thread.getState())
                || Objects.equals("TIMED_WAITING", thread.getState());
        if (waiting && matchWait(thread, "connectionWait")) {
            return;
        }
        if ((waiting || Objects.equals("RUNNABLE", thread.getState())) && matchWait(thread, "networkRead")) {
            return;
        }
        if (waiting && matchWait(thread, "timerWait")) {
            return;
        }
        if (waiting) {
            matchWait(thread, "taskWait");
        }
    }

    /** 分类必须匹配具体类和方法，线程名称不作为诊断依据。 */
    private boolean matchWait(ThreadSnapshotThreadVo thread, String type) {
        for (ThreadSnapshotFrameVo frame : thread.getStackTrace()) {
            if (matches(frame, type)) {
                thread.setWaitType(type);
                thread.setWaitEvidence(frame.getText());
                return true;
            }
        }
        return false;
    }

    /** 使用精确栈帧模式识别常见等待，保留匹配证据供用户核对。 */
    private boolean matches(ThreadSnapshotFrameVo frame, String type) {
        String clazz = frame.getClassName();
        String method = frame.getMethodName();
        if ("connectionWait".equals(type)) {
            return frameIs(clazz, method, "com.zaxxer.hikari.pool.HikariPool", "getConnection")
                    || frameIs(clazz, method, "com.zaxxer.hikari.util.ConcurrentBag", "borrow")
                    || frameIs(clazz, method, "org.apache.commons.pool2.impl.GenericObjectPool", "borrowObject");
        }
        if ("networkRead".equals(type)) {
            return frameIs(clazz, method, "java.net.SocketInputStream", "socketRead0")
                    || frameIs(clazz, method, "java.net.SocketInputStream", "socketRead")
                    || frameIs(clazz, method, "sun.nio.ch.SocketDispatcher", "read0")
                    || frameIs(clazz, method, "sun.nio.ch.SocketDispatcher", "read")
                    || frameIs(clazz, method, "sun.nio.ch.SocketChannelImpl", "read")
                    || frameIs(clazz, method, "sun.nio.ch.NioSocketImpl", "implRead")
                    || frameIs(clazz, method, "sun.nio.ch.NioSocketImpl", "read");
        }
        if ("timerWait".equals(type)) {
            return frameIs(clazz, method, "java.lang.Thread", "sleep")
                    || frameIs(clazz, method, "java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue", "take")
                    || frameIs(clazz, method, "java.util.TimerThread", "mainLoop")
                    || frameIs(clazz, method, "org.quartz.core.QuartzSchedulerThread", "run");
        }
        return frameIs(clazz, method, "java.util.concurrent.ThreadPoolExecutor", "getTask")
                || frameIs(clazz, method, "org.quartz.simpl.SimpleThreadPool$WorkerThread", "run")
                || frameIs(clazz, method, "java.util.concurrent.ForkJoinPool", "awaitWork");
    }

    /** 比较调用帧中的类名和方法名，不依赖行号或线程命名约定。 */
    private boolean frameIs(String clazz, String method, String expectedClass, String expectedMethod) {
        return Objects.equals(clazz, expectedClass) && Objects.equals(method, expectedMethod);
    }

    /** 死锁使用 JVM 结论，单次锁竞争为信息，线程池满载并排队为线索。 */
    private List<ThreadSnapshotDiagnosticVo> diagnose(ThreadSnapshotVo snapshot) {
        List<ThreadSnapshotDiagnosticVo> result = new ArrayList<>();
        if (!snapshot.getDeadlockedThreadIds().isEmpty()) {
            ThreadSnapshotDiagnosticVo deadlock = diagnostic("deadlock", "confirmed");
            deadlock.setThreadIds(new ArrayList<>(snapshot.getDeadlockedThreadIds()));
            for (ThreadSnapshotThreadVo thread : snapshot.getThreads()) {
                if (deadlock.getThreadIds().contains(thread.getId())) {
                    deadlock.getEvidence().add(lockEvidence(thread));
                }
            }
            result.add(deadlock);
        }
        Map<String, Map<String, ThreadSnapshotDiagnosticVo>> locks = new LinkedHashMap<>();
        for (ThreadSnapshotThreadVo thread : snapshot.getThreads()) {
            // ReentrantLock 竞争通常表现为 WAITING；仅有明确持有者时建立等待关系。
            boolean lockWaiting = Objects.equals("BLOCKED", thread.getState())
                    || Objects.equals("WAITING", thread.getState())
                    || Objects.equals("TIMED_WAITING", thread.getState());
            if (lockWaiting && thread.getLockOwnerId() != null && !thread.getLockOwnerId().isEmpty()
                    && !Objects.equals("-1", thread.getLockOwnerId()) && thread.getLockName() != null) {
                Map<String, ThreadSnapshotDiagnosticVo> ownerLocks = locks.computeIfAbsent(
                        thread.getLockOwnerId(), ignored -> new LinkedHashMap<>());
                ThreadSnapshotDiagnosticVo lock = ownerLocks.get(thread.getLockName());
                if (lock == null) {
                    lock = diagnostic("lockContention", "informational");
                    lock.setOwnerId(thread.getLockOwnerId());
                    lock.setLockName(thread.getLockName());
                    ownerLocks.put(thread.getLockName(), lock);
                    result.add(lock);
                }
                lock.getThreadIds().add(thread.getId());
                lock.getEvidence().add(lockEvidence(thread));
            }
        }
        ThreadPoolVo pool = snapshot.getThreadPool();
        int active = pool.getMainActiveCount() + pool.getBackupActiveCount();
        int queued = pool.getMainQueueSize() + pool.getBackupQueueSize();
        if (pool.getMaxThreadCount() > 0 && active >= pool.getMaxThreadCount() && queued > 0) {
            ThreadSnapshotDiagnosticVo pressure = diagnostic("poolPressure", "suspect");
            pressure.getEvidence().add("active=" + active);
            pressure.getEvidence().add("max=" + pool.getMaxThreadCount());
            pressure.getEvidence().add("queued=" + queued);
            for (ThreadTaskVo task : pool.getThreadTaskList()) {
                if (task.getId() != null) {
                    pressure.getThreadIds().add(String.valueOf(task.getId()));
                }
            }
            result.add(pressure);
        }
        return result;
    }

    /** 建立提示码和级别，界面负责翻译文案。 */
    private ThreadSnapshotDiagnosticVo diagnostic(String code, String severity) {
        ThreadSnapshotDiagnosticVo result = new ThreadSnapshotDiagnosticVo();
        result.setCode(code);
        result.setSeverity(severity);
        return result;
    }

    /** 保存原始线程和锁标识，避免将累计次数误解释为阻塞时长。 */
    private String lockEvidence(ThreadSnapshotThreadVo thread) {
        return "thread=" + thread.getId() + ",state=" + thread.getState()
                + ",lock=" + thread.getLockName() + ",owner=" + thread.getLockOwnerId();
    }

    /** 快照内线程池线程的运行时长固定在采集时，不随 JSON 导出时间增长。 */
    private static class FrozenPoolThreadVo extends ThreadVo {
        private final long snapshotTimeCost;

        /** 复制线程池线程，不共享可变日期对象。 */
        private FrozenPoolThreadVo(ThreadVo source, long capturedAt) {
            super(source.getId(), source.getName());
            if (source.getStartTime() != null) {
                setStartTime(new Date(source.getStartTime().getTime()));
                snapshotTimeCost = Math.max(0L, capturedAt - getStartTime().getTime());
            } else {
                // 状态对象理论上始终包含开始时间，兼容缺失字段时使用采集时间。
                setStartTime(new Date(capturedAt));
                snapshotTimeCost = 0L;
            }
        }

        /** 读取采集时固定值，不查询当前时钟。 */
        @Override
        public long getTimeCost() {
            return snapshotTimeCost;
        }
    }
}
