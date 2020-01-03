package codedriver.module.tenant.api.counter;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.counter.GlobalCounterService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @program: codedriver
 * @description:
 * @create: 2019-12-10 12:12
 **/
@Service
public class CounterResortApi extends ApiComponentBase {

    @Autowired
    private GlobalCounterService counterService;

    @Override
    public String getToken() {
        return "globalCounter/counterReSort";
    }

    @Override
    public String getName() {
        return "消息统计重排序接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param(name = "sortPluginIdStr", type = ApiParamType.STRING, desc = "排序后的pluginId拼接字符串,使用“,”隔开", isRequired = true)})
    @Description(desc = "统计消息重排序接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String sortIdStr = jsonObj.getString("sortPluginIdStr");
        counterService.updateCounterUserSort(UserContext.get().getUserId(), sortIdStr);
        return new JSONObject();
    }
}