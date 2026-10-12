package neatlogic.module.tenant.service.healthcheck;

import neatlogic.framework.dto.healthcheck.ThreadSnapshotVo;

/** 采集并关联业务上下文，提供有明确证据的单次线程诊断。 */
public interface ThreadSnapshotService {
    /** 获取当前节点的一次线程快照，不主动启用 JVM 监控开关。 */
    ThreadSnapshotVo capture();
}
