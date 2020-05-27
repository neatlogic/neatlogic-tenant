package codedriver.module.tenant.api.reminder;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.reminder.core.GlobalReminderHandlerFactory;
import codedriver.framework.reminder.core.IGlobalReminderHandler;
import codedriver.framework.reminder.dao.mapper.GlobalReminderMessageMapper;
import codedriver.framework.reminder.dto.GlobalReminderMessageVo;
import codedriver.framework.reminder.dto.ReminderMessageSearchVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.reminder.GlobalReminderService;

/**
 * @program: codedriver
 * @description:
 * @create: 2019-12-10 11:39
 **/
@Service
public class DayReminderApi extends ApiComponentBase {

	@Autowired
	private GlobalReminderMessageMapper reminderMessageMapper;
	
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
        List<GlobalReminderMessageVo> messageList = getDayReminderMessageVoList(messageId, day);
        JSONArray messageArray = new JSONArray();
        for (GlobalReminderMessageVo messageVo : messageList){
            IGlobalReminderHandler reminder = GlobalReminderHandlerFactory.getReminder(messageVo.getReminderVo().getHandler());
            messageArray.add(reminder.packData(messageVo));
        }
        Long lastMessageId = 0L;
        if (messageList != null && messageList.size() > 0){
            lastMessageId = messageList.get(messageList.size() - 1).getId();
        }
        Map<String, String> timeMap = reminderService.getTimeMap(day);
        ReminderMessageSearchVo searchVo = new ReminderMessageSearchVo();
        searchVo.setUserUuid(UserContext.get().getUserUuid(true));
        searchVo.setStartTime(timeMap.get("startTime"));
        searchVo.setEndTime(timeMap.get("endTime"));
        returnJson.put("messageArray", messageArray);
        returnJson.put("messageCount", messageList.size() + messageCount);
        returnJson.put("lastMessageId", lastMessageId);
        returnJson.put("showDay", getShowDay(day));
        returnJson.put("day", day);
        returnJson.put("allMessageCount", reminderMessageMapper.getReminderMessageCountByDay(searchVo));
        return returnJson;
    }

    public String getShowDay(Integer day){
        if (day == 0){
            return "今天";
        }
        return day + "天前";
    }
    
    /** 
     * @Description: 获取更多实时动态消息
     * @Param: [messageId, day] 
     * @return: java.util.List<com.techsure.balantflow.dto.globalreminder.GlobalReminderMessageVo>  
     */ 
    public List<GlobalReminderMessageVo> getDayReminderMessageVoList(Long messageId, Integer day) {
        Map<String, String> timeMap = reminderService.getTimeMap(day);
        ReminderMessageSearchVo searchVo = new ReminderMessageSearchVo();
        if (messageId != null && messageId != 0L){
            searchVo.setMessageCount(ReminderMessageSearchVo.DEFAULT_ADD_COUNT);
            searchVo.setMessageId(messageId);
        }else {
            searchVo.setMessageCount(ReminderMessageSearchVo.DEFAULT_SHOW_COUNT);
        }
        searchVo.setStartTime(timeMap.get("startTime"));
        searchVo.setEndTime(timeMap.get("endTime"));
        searchVo.setUserUuid(UserContext.get().getUserUuid(true));
        List<GlobalReminderMessageVo> messageVoList = reminderMessageMapper.getShowReminderMessageListByIdListAndUserUuid(searchVo);
        for (GlobalReminderMessageVo messageVo : messageVoList){
        	reminderService.packageData(messageVo);
        }
        return messageVoList;
    }
    
}
