package codedriver.module.tenant.api.reminder;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.reminder.dto.GlobalReminderSubscribeVo;
import codedriver.module.tenant.service.reminder.GlobalReminderService;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @program: codedriver
 * @description:
 * @create: 2019-12-10 11:30
 **/
@Service
public class ReminderSubscribeApi extends ApiComponentBase {

    @Autowired
    private GlobalReminderService reminderService;

    @Override
    public String getToken() {
        return "globalReminder/subscribe";
    }

    @Override
    public String getName() {
        return "实时动态订阅接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param(name = "id", type = ApiParamType.LONG, desc = "数据主键ID"),
             @Param(name = "pluginId", type = ApiParamType.STRING, desc = "实时动态插件ID", isRequired = true),
             @Param(name = "param", type = ApiParamType.JSONOBJECT, desc = "插件配置参数", isRequired = true),
             @Param(name = "isActive", type = ApiParamType.INTEGER, desc = "插件状态", isRequired = true)})
    @Output({ @Param(name = "id", type = ApiParamType.LONG, desc = "订阅主键ID")})
    @Description(desc = "实时动态订阅接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        GlobalReminderSubscribeVo reminderSubscribe = new GlobalReminderSubscribeVo();
        Long id = 0L;
        if (jsonObj.containsKey("id")){
            id = jsonObj.getLong("id");
        }
        String pluginId = jsonObj.getString("pluginId");
        JSONObject paramObj = jsonObj.getJSONObject("param");
        int isActive = jsonObj.getInteger("isActive");
        reminderSubscribe.setUserUuid(UserContext.get().getUserUuid(true));
        reminderSubscribe.setPluginId(pluginId);
        reminderSubscribe.setId(id);
        reminderSubscribe.setParam(paramObj.toJSONString());
        reminderSubscribe.setIsActive(isActive);
        JSONObject returnJson = new JSONObject();
        reminderService.updateReminderSubscribe(reminderSubscribe);
        returnJson.put("id", reminderSubscribe.getId());
        return returnJson;
    }
}
