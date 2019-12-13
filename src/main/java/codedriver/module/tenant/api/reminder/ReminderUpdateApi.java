package codedriver.module.tenant.api.reminder;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.reminder.service.GlobalReminderService;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.restful.annotation.Input;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @program: codedriver
 * @description:
 * @create: 2019-12-10 11:51
 **/
@Service
public class ReminderUpdateApi extends ApiComponentBase {

    @Autowired
    private GlobalReminderService reminderService;

    @Override
    public String getToken() {
        return "globalReminder/updateActive";
    }

    @Override
    public String getName() {
        return "重置消息有效性接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param(name = "id", type = ApiParamType.LONG, desc = "消息主键ID", isRequired = true)})
    @Description(desc = "重置消息有效性接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        reminderService.updateMessageActive(jsonObj.getLong("id"), UserContext.get().getUserId());
        return new JSONObject();
    }
}
