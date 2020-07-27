package codedriver.module.tenant.api.reminder;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.reminder.dao.mapper.GlobalReminderMapper;
import codedriver.framework.reminder.dao.mapper.GlobalReminderMessageMapper;
import codedriver.framework.reminder.dto.GlobalReminderSubscribeVo;
import codedriver.framework.restful.core.ApiComponentBase;

/**
 * @program: codedriver
 * @description:
 * @create: 2019-12-10 11:30
 **/
@Service
@OperationType(type = OperationTypeEnum.CREATE)
public class ReminderSubscribeApi extends ApiComponentBase {

    @Autowired
    private GlobalReminderMapper reminderMapper;

    @Autowired
    private GlobalReminderMessageMapper reminderMessageMapper;

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
             @Param(name = "handler", type = ApiParamType.STRING, desc = "实时动态插件ID", isRequired = true),
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
        String handler = jsonObj.getString("handler");
        JSONObject paramObj = jsonObj.getJSONObject("param");
        int isActive = jsonObj.getInteger("isActive");
        reminderSubscribe.setUserUuid(UserContext.get().getUserUuid(true));
        reminderSubscribe.setHandler(handler);
        reminderSubscribe.setId(id);
        reminderSubscribe.setParam(paramObj.toJSONString());
        reminderSubscribe.setIsActive(isActive);
        JSONObject returnJson = new JSONObject();
        if (reminderSubscribe.getId() != null){
                if (isActive == 1){
                reminderMapper.updateReminderSubscribe(reminderSubscribe);
            }else {
                reminderMapper.deleteReminderSubscribe(reminderSubscribe);
                //取消订阅，移除该控件所有消息的有效性
                reminderMessageMapper.updateMessageActiveByReminderId(reminderSubscribe.getUserUuid(), reminderSubscribe.getHandler());
            }
        }else {
            reminderMapper.insertReminderSubscribe(reminderSubscribe);
        }
        returnJson.put("id", reminderSubscribe.getId());
        return returnJson;
    }
}
