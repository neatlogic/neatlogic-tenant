package neatlogic.module.tenant.api.globallock;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.globallock.GlobalLockManager;
import neatlogic.framework.globallock.core.GlobalLockHandlerFactory;
import neatlogic.framework.globallock.core.IGlobalLockHandler;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Service
@OperationType(type = OperationTypeEnum.OPERATE)
public class GlobalLockApi extends PrivateApiComponentBase {

    @Resource
    private FormMapper formMapper;

    @Override
    public String getToken() {
        return "global/lock";
    }

    @Override
    public String getName() {
        return "全局锁";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "operType", type = ApiParamType.ENUM, rule = "auto,deploy", desc = "来源类型"),
            @Param(name = "action", type = ApiParamType.ENUM, rule = "lock,unlock,cancel,retry", isRequired = true, desc = "执行动作"),
            @Param(name = "lockId", type = ApiParamType.LONG, desc = "锁id")
    })
    @Output({
            @Param(name = "lockId", type = ApiParamType.LONG, desc = "锁id"),
            @Param(name = "wait", type = ApiParamType.LONG, desc = "0：获取锁成功；1：进入等待队列"),
            @Param(name = "message", type = ApiParamType.LONG, desc = "wait 原因"),
    })
    @Description(desc = "全局锁接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String action = jsonObj.getString("action");
        Long lockId = jsonObj.getLong("lockId");
        String handler = jsonObj.getString("operType");

        if (Objects.equals(action, "cancel")) {
            GlobalLockManager.cancelLock(lockId);
        } else {
            IGlobalLockHandler globalLockHandler = GlobalLockHandlerFactory.getHandler(handler);
            switch (action) {
                case "lock":
                    return globalLockHandler.getLock(jsonObj);
                case "unlock":
                    return globalLockHandler.unLock(lockId, jsonObj);
                case "retry":
                    globalLockHandler.retryLock(lockId, jsonObj);
                    break;
            }
        }
        return null;
    }
}
