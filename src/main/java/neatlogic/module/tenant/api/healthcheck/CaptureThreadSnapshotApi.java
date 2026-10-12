package neatlogic.module.tenant.api.healthcheck;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.service.healthcheck.ThreadSnapshotService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/** 管理员采集结构化线程快照，保留原有文本转储接口。 */
@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class CaptureThreadSnapshotApi extends PrivateApiComponentBase {
    @Resource
    private ThreadSnapshotService threadSnapshotService;

    /** 返回同一次采集的 JVM 线程、线程池任务和诊断依据。 */
    @Description(desc = "nmtah.capturethreadsnapshotapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) {
        return threadSnapshotService.capture();
    }

    /** 注册结构化快照入口。 */
    @Override
    public String getToken() {
        return "/healthcheck/thread/snapshot";
    }

    /** 使用租户模块语言资源展示接口名称。 */
    @Override
    public String getName() {
        return "nmtah.capturethreadsnapshotapi.getname";
    }
}
