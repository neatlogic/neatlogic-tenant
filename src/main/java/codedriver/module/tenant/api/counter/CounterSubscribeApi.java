package codedriver.module.tenant.api.counter;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.counter.dto.GlobalCounterSubscribeVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.restful.annotation.Param;
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
public class CounterSubscribeApi extends ApiComponentBase {

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
        counterSubscribeVo.setUserId(UserContext.get().getUserId());
        JSONObject returnJson = new JSONObject();
        counterService.updateCounterSubscribe(counterSubscribeVo);
        returnJson.put("id", counterSubscribeVo.getId());
        return returnJson;
    }
}
