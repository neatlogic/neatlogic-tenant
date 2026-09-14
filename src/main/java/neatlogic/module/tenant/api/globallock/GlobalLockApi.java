package neatlogic.module.tenant.api.globallock;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.systemuser.SystemUser;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.util.$;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.globallock.GlobalLockManager;
import neatlogic.framework.globallock.core.GlobalLockHandlerFactory;
import neatlogic.framework.globallock.core.IGlobalLockHandler;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;
import neatlogic.framework.globallock.GlobalLockOperationManager;

@Service
@AuthUser(SystemUser.AUTOEXEC)
@OperationType(type = OperationTypeEnum.OPERATE)
@AuthAction(action = NoAuth.class)
public class GlobalLockApi extends PrivateApiComponentBase {

    @Resource
    private GlobalLockOperationManager operations;

    @Override
    public String getToken() {
        return "global/lock";
    }

    @Override
    public String getName() {
        return "globallock.api";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "operType", type = ApiParamType.ENUM, rule = "auto,deploy", desc = "globallock.handler"),
            @Param(name = "action", type = ApiParamType.ENUM, rule = "lock,unlock,cancel,retry", isRequired = true, desc = "globallock.action"),
            @Param(name = "lockId", type = ApiParamType.LONG, desc = "globallock.lockid"),
            @Param(name = "operationId", type = ApiParamType.STRING, desc = "globallock.operationid")
    })
    @Output({
            @Param(name = "lockId", type = ApiParamType.LONG, desc = "globallock.lockid"),
            @Param(name = "wait", type = ApiParamType.LONG, desc = "globallock.wait"),
            @Param(name = "message", type = ApiParamType.STRING, desc = "globallock.waitreason"),
    })
    @Description(desc = "globallock.api")
    /** 校验请求并执行当前租户范围内的锁操作。 */
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String action = jsonObj.getString("action");
        Long lockId = jsonObj.getLong("lockId");
        String handler = jsonObj.getString("operType");
        String operationId = jsonObj.getString("operationId");
        if (operationId != null) {
            if (!("unlock".equals(action) || "cancel".equals(action))) throw new ParamIrregularException("action",
                    $.t("globallock.error.operationaction", operationId, lockId, action));
            if (!operations.claim(operationId, lockId, action)) return operations.get(operationId);
            GlobalLockManager.release(lockId, jsonObj, "unlock".equals(action));
            return operations.get(operationId);
        }
        if (Objects.equals(action, "cancel")) {
            GlobalLockManager.cancelLock(lockId);
        } else {
            IGlobalLockHandler globalLockHandler = GlobalLockHandlerFactory.getHandler(handler);
            if(globalLockHandler == null){
                throw new ParamIrregularException("operType", $.t("globallock.error.handlerunknown", handler, lockId, action));
            }
            switch (action) {
                case "lock":
                    return globalLockHandler.getLock(jsonObj);
                case "unlock":
                    return globalLockHandler.unLock(lockId, jsonObj);
                case "retry":
                    return globalLockHandler.retryLock(lockId, jsonObj);
                default: throw new ParamIrregularException("action", $.t("globallock.error.actioninvalid", action, lockId, handler));
            }
        }
        return null;
    }
}
