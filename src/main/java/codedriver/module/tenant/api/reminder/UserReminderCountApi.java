package codedriver.module.tenant.api.reminder;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.core.constvalue.OperationTypeEnum;
import codedriver.framework.reminder.dao.mapper.GlobalReminderMessageMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @program: codedriver
 * @description:
 * @create: 2019-12-10 11:59
 **/
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class UserReminderCountApi extends PrivateApiComponentBase {

    @Autowired
    private GlobalReminderMessageMapper reminderMessageMapper;

    @Override
    public String getToken() {
        return "globalReminder/getRealTimeMessageCount";
    }

    @Override
    public String getName() {
        return "获取实时动态消息数量接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({ @Param(name = "messageCount", type = ApiParamType.INTEGER, desc = "实时动态消息数量")})
    @Description(desc = "获取实时动态消息数量接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        returnObj.put("messageCount", reminderMessageMapper.getReminderMessageCount(UserContext.get().getUserUuid()));
        return returnObj;
    }
}
