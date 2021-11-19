/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.healthcheck;

import codedriver.framework.asynchronization.threadlocal.RequestContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.ADMIN;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class ThreadDumpApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "/healthcheck/threaddump";
    }

    @Override
    public String getName() {
        return "打印线程快照";
    }

    @Override
    public String getConfig() {
        return null;
    }

    private static void dumpTraces(ThreadMXBean mxBean, Map<Long, ThreadInfo> threadInfoMap, Writer writer) throws IOException {
        Map<Thread, StackTraceElement[]> stacks = Thread.getAllStackTraces();
        long now = System.currentTimeMillis();
        writer.write("=================" + stacks.size() + " thread of " + RequestContext.get().getRequest().getLocalAddr() + " at " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z").format(new Date(now)) + " start.=================\n\n");
        for (Map.Entry<Thread, StackTraceElement[]> entry : stacks.entrySet()) {
            Thread thread = entry.getKey();
            writer.write("\"" + thread.getName() + "\" prio=" + thread.getPriority() + " tid=" + thread.getId() + " " + thread.getState() + " " + (thread.isDaemon() ? "deamon" : "worker"));
            ThreadInfo threadInfo = threadInfoMap.get(thread.getId());
            if (threadInfo != null) {
                writer.write(" native=" + threadInfo.isInNative() + ", suspended=" + threadInfo.isSuspended() + ", block=" + threadInfo.getBlockedCount() + ", wait=" + threadInfo.getWaitedCount());
                writer.write(" lock=" + threadInfo.getLockName() + " owned by " + threadInfo.getLockOwnerName() + " (" + threadInfo.getLockOwnerId() + "), cpu=" + (mxBean.getThreadCpuTime(threadInfo.getThreadId()) / 1000000L) + ", user=" + (mxBean.getThreadUserTime(threadInfo.getThreadId()) / 1000000L) + "\n");
            }
            for (StackTraceElement element : entry.getValue()) {
                writer.write("\t\t");
                writer.write(element.toString());
                writer.write("\n");
            }
            writer.write("\n");
        }
        writer.write("=================" + stacks.size() + " thread of " + RequestContext.get().getUrl() + " at " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z").format(new Date(now)) + " end.=================\n\n");

    }

    @Description(desc = "打印线程快照接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threadInfos = mxBean.getThreadInfo(mxBean.getAllThreadIds(), 0);
        Map<Long, ThreadInfo> threadInfoMap = new HashMap<>();
        for (ThreadInfo threadInfo : threadInfos) {
            threadInfoMap.put(threadInfo.getThreadId(), threadInfo);
        }

        StringWriter writer = new StringWriter();
        dumpTraces(mxBean, threadInfoMap, writer);
        return writer.toString();
    }
}
