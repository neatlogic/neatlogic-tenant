package codedriver.module.tenant.api.counter;


import codedriver.framework.dto.ModuleVo;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.service.counter.GlobalCounterService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @program: codedriver
 * @description:
 * @create: 2019-12-10 12:06
 **/
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class CounterModuleListApi extends PrivateApiComponentBase {

    @Autowired
    private GlobalCounterService counterService;

    @Override
    public String getToken() {
        return "globalCounter/counterModuleList";
    }

    @Override
    public String getName() {
        return "查询消息统计模块列表接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({ @Param(explode = ModuleVo.class),
              @Param(name = "counterModuleList", explode = ModuleVo[].class, desc = "模块列表")})
    @Description(desc = "查询消息统计模块列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnJson = new JSONObject();
        returnJson.put("counterModuleList", counterService.getActiveCounterModuleList());
        return returnJson;
    }
}
