package codedriver.module.tenant.api.globallock;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.type.ParamIrregularException;
import codedriver.framework.form.dao.mapper.FormMapper;
import codedriver.framework.globallock.GlobalLockManager;
import codedriver.framework.globallock.core.GlobalLockHandlerFactory;
import codedriver.framework.globallock.core.IGlobalLockHandler;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
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
        if (StringUtils.isBlank(handler)) {
            if (Objects.equals(action, "cancel")) {
                GlobalLockManager.cancelLock(lockId);
            } else {
                throw new ParamIrregularException("operType");
            }
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
