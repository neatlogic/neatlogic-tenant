package codedriver.module.tenant.api.reminder;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.reminder.dto.GlobalReminderMessageVo;
import codedriver.framework.reminder.core.GlobalReminderFactory;
import codedriver.framework.reminder.core.IGlobalReminder;
import codedriver.framework.reminder.service.GlobalReminderService;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.restful.annotation.Param;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2019-12-10 11:41
 **/
@Service
public class ScheduleReminderApi extends ApiComponentBase {

    @Autowired
    private GlobalReminderService reminderService;

    @Override
    public String getToken() {
        return "globalReminder/scheduleDayMessage";
    }

    @Override
    public String getName() {
        return "定时获取新消息接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({ @Param(name = "scheduleMessageList", type = ApiParamType.JSONARRAY, desc = "消息集合"),
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
              @Param(name = "messageArray[].popUpTemplate", type = ApiParamType.STRING, desc = "消息弹窗模板路径")})
    @Description(desc = "定时获取新消息接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        List<GlobalReminderMessageVo> messageVos = reminderService.getScheduleMessageList(UserContext.get().getUserId());
        Collections.sort(messageVos);
        JSONArray messageArray = new JSONArray();
        for (GlobalReminderMessageVo messageVo : messageVos){
            IGlobalReminder reminder = GlobalReminderFactory.getReminder(messageVo.getReminderVo().getPluginId());
            messageArray.add(reminder.packData(messageVo));
        }
        returnObj.put("scheduleMessageList", messageArray);
        return returnObj;
    }
}
