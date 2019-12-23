package codedriver.module.tenant.api.reminder;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.reminder.dto.GlobalReminderMessageVo;
import codedriver.framework.reminder.core.GlobalReminderFactory;
import codedriver.framework.reminder.core.IGlobalReminder;
import codedriver.module.tenant.service.reminder.GlobalReminderService;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2019-12-10 11:39
 **/
@Service
public class DayReminderApi extends ApiComponentBase {

    @Autowired
    private GlobalReminderService reminderService;

    @Override
    public String getToken() {
        return "globalReminder/dayMessage";
    }

    @Override
    public String getName() {
        return "查询当天消息列表接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param(name = "messageId", type = ApiParamType.LONG, desc = "起始消息ID"),
             @Param(name = "day", type = ApiParamType.INTEGER, desc = "日期定位，0代表当天", isRequired = true),
             @Param(name = "messageCount", type = ApiParamType.INTEGER, desc = "消息累计总量", isRequired = true)})
    @Output({ @Param(name = "messageArray", type = ApiParamType.JSONARRAY, desc = "消息集合"),
              @Param(name = "messageArray[].id", type = ApiParamType.LONG, desc = "消息ID"),
              @Param(name = "messageArray[].title", type = ApiParamType.STRING, desc = "消息标题"),
              @Param(name = "messageArray[].content", type = ApiParamType.STRING, desc = "消息内容"),
              @Param(name = "messageArray[].createTime", type = ApiParamType.STRING, desc = "消息产生时间"),
              @Param(name = "messageArray[].fromUserName", type = ApiParamType.STRING, desc = "消息发送人"),
              @Param(name = "messageArray[].remindName", type = ApiParamType.STRING, desc = "消息插件名称"),
              @Param(name = "messageArray[].moduleName", type = ApiParamType.STRING, desc = "插件模块ID"),
              @Param(name = "messageArray[].moduleIcon", type = ApiParamType.STRING, desc = "插件模块图标"),
              @Param(name = "messageArray[].receiver", type = ApiParamType.STRING, desc = "消息收件人ID"),
              @Param(name = "messageArray[].receiverName", type = ApiParamType.STRING, desc = "消息收件人名称"),
              @Param(name = "messageArray[].isKeep", type = ApiParamType.INTEGER, desc = "1 表示 持续，0 表示 临时"),
              @Param(name = "messageArray[].showTemplate", type = ApiParamType.STRING, desc = "消息展示模板路径"),
              @Param(name = "messageArray[].popUpTemplate", type = ApiParamType.STRING, desc = "消息弹窗模板路径"),
              @Param(name = "messageCount", type = ApiParamType.INTEGER, desc = "消息数量"),
              @Param(name = "lastMessageId", type = ApiParamType.LONG, desc = "最新消息ID"),
              @Param(name = "showDay", type = ApiParamType.STRING, desc = "时间展示"),
              @Param(name = "day", type = ApiParamType.INTEGER, desc = "天数"),
              @Param(name = "allMessageCount", type = ApiParamType.INTEGER, desc = "消息总数")})
    @Description(desc = "查询当天消息列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        long messageId = 0L;
        if (jsonObj.containsKey("messageId")){
            messageId = jsonObj.getLong("messageId");
        }
        int day = jsonObj.getInteger("day");
        int messageCount = jsonObj.getInteger("messageCount");
        JSONObject returnJson = new JSONObject();
        String userId = UserContext.get().getUserId();
        List<GlobalReminderMessageVo> messageList = reminderService.getDayReminderMessageVoListByUserId(userId, messageId, day);
        JSONArray messageArray = new JSONArray();
        for (GlobalReminderMessageVo messageVo : messageList){
            IGlobalReminder reminder = GlobalReminderFactory.getReminder(messageVo.getReminderVo().getPluginId());
            messageArray.add(reminder.packData(messageVo));
        }
        Long lastMessageId = 0L;
        if (messageList != null && messageList.size() > 0){
            lastMessageId = messageList.get(messageList.size() - 1).getId();
        }
        returnJson.put("messageArray", messageArray);
        returnJson.put("messageCount", messageList.size() + messageCount);
        returnJson.put("lastMessageId", lastMessageId);
        returnJson.put("showDay", getShowDay(day));
        returnJson.put("day", day);
        returnJson.put("allMessageCount", reminderService.getReminderMessageCountByDay(day, userId));
        return returnJson;
    }

    public String getShowDay(Integer day){
        if (day == 0){
            return "今天";
        }
        return day + "天前";
    }
}
