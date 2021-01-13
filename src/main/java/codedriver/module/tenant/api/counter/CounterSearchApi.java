package codedriver.module.tenant.api.counter;

import codedriver.framework.counter.dto.GlobalCounterVo;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.counter.core.GlobalCounterFactory;
import codedriver.framework.counter.core.IGlobalCounter;
import codedriver.framework.restful.core.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.service.counter.GlobalCounterService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2019-12-10 12:05
 **/
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class CounterSearchApi extends PrivateApiComponentBase {

    @Autowired
    private GlobalCounterService counterService;

    @Override
    public String getToken() {
        return "globalCounter/search";
    }

    @Override
    public String getName() {
        return "消息统计插件检索接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param(name = "moduleId", type = ApiParamType.STRING, desc = "模块ID")})
    @Output({ @Param(explode = GlobalCounterVo.class),
              @Param(name = "counterList", explode = GlobalCounterVo[].class, desc = "消息统计插件列表")})
    @Description(desc = "消息统计插件检索接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        GlobalCounterVo counter = new GlobalCounterVo();
        JSONObject returnJson = new JSONObject();
        if(jsonObj.containsKey("moduleId")){
            String moduleId = jsonObj.getString("moduleId");
            counter.setModuleId(moduleId);
        }
        List<GlobalCounterVo> counterList = counterService.searchCounterVo(counter);
        if (counterList != null && counterList.size() > 0){
            for (int i = 0; i < counterList.size(); i++){
                GlobalCounterVo counterVo = counterList.get(i);
                IGlobalCounter globalCounter = GlobalCounterFactory.getCounter(counterVo.getPluginId());
                counterVo.setPreviewPath(globalCounter.getPreviewPath());
                counterVo.setShowTemplate(globalCounter.getShowTemplate());
            }
        }
        Collections.sort(counterList);
        returnJson.put("counterList", counterList);
        return returnJson;
    }
}
