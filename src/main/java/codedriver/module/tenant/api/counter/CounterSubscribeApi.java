package codedriver.module.tenant.api.counter;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.counter.dto.GlobalCounterSubscribeVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.auth.label.MESSAGE_CENTER_MODIFY;
import codedriver.module.tenant.service.counter.GlobalCounterService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @program: codedriver
 * @description:
 * @create: 2019-12-10 12:10
 **/
@Service
@AuthAction(action = MESSAGE_CENTER_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class CounterSubscribeApi extends PrivateApiComponentBase {

    @Autowired
    private GlobalCounterService counterService;

    @Override
    public String getToken() {
        return "globalCounter/subscribe";
    }

    @Override
    public String getName() {
        return "消息统计模块订阅接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param(name = "id", type = ApiParamType.LONG, desc = "主键ID"),
             @Param(name = "pluginId", type = ApiParamType.STRING, desc = "消息统计插件ID", isRequired = true)})
    @Output({ @Param(name = "id", type = ApiParamType.LONG, desc = "主键ID")})
    @Description(desc = "消息统计模块订阅接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = 0L;
        if (jsonObj.containsKey("id")){
            id = jsonObj.getLong("id");
        }
        String counterId = jsonObj.getString("pluginId");
        GlobalCounterSubscribeVo counterSubscribeVo = new GlobalCounterSubscribeVo();
        counterSubscribeVo.setId(id);
        counterSubscribeVo.setPluginId(counterId);
        counterSubscribeVo.setUserUuid(UserContext.get().getUserUuid(true));
        JSONObject returnJson = new JSONObject();
        counterService.updateCounterSubscribe(counterSubscribeVo);
        returnJson.put("id", counterSubscribeVo.getId());
        return returnJson;
    }
}
