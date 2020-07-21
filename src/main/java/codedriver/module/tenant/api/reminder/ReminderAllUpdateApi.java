package codedriver.module.tenant.api.reminder;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.reminder.dao.mapper.GlobalReminderMessageMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
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
@OperationType(type = OperationTypeEnum.UPDATE)
public class ReminderAllUpdateApi extends ApiComponentBase {

    @Autowired
    private GlobalReminderMessageMapper reminderMessageMapper;

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
        reminderMessageMapper.updateAllMessageActive(UserContext.get().getUserUuid());
        return null;
    }
}
