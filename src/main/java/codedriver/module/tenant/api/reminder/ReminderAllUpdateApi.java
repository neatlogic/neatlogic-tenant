package codedriver.module.tenant.api.reminder;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.module.tenant.service.reminder.GlobalReminderService;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.core.ApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @program: codedriver
 * @description:
 * @create: 2019-12-10 11:54
 **/
@Service
public class ReminderAllUpdateApi extends ApiComponentBase {

    @Autowired
    private GlobalReminderService reminderService;

    @Override
    public String getToken() {
        return "globalReminder/updateAllActive";
    }

    @Override
    public String getName() {
        return "重置所有消息有效性接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Description(desc = "重置所有消息有效性接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String userId = UserContext.get().getUserId();
        reminderService.updateAllMessageActive(userId);
        return new JSONObject();
    }
}
