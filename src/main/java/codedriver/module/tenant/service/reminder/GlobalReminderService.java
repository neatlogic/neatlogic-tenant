package codedriver.module.tenant.service.reminder;

import codedriver.framework.dto.ModuleVo;
import codedriver.framework.reminder.dto.GlobalReminderMessageVo;
import codedriver.framework.reminder.dto.GlobalReminderSubscribeVo;
import codedriver.framework.reminder.dto.GlobalReminderVo;
import codedriver.framework.reminder.dto.param.ReminderHistoryParamVo;

import java.util.List;

public interface GlobalReminderService {
    
    /** 
    * @Description: 检索实时动态插件
    * @Param: [reminderVo] 
    * @return: java.util.List<com.techsure.balantflow.dto.globalreminder.GlobalReminderVo>  
    */ 
    List<GlobalReminderVo> searchReminder(GlobalReminderVo reminderVo);

    /** 
    * @Description: 获取实时动态模块集合
    * @Param: [] 
    * @return: java.util.List<com.techsure.balantflow.dto.ModuleVo>  
    */ 
    List<ModuleVo> getActiveReminderModuleList();

    /** 
    * @Description: 获取更多实时动态消息
    * @Param: [messageId, day] 
    * @return: java.util.List<com.techsure.balantflow.dto.globalreminder.GlobalReminderMessageVo>  
    */ 
    List<GlobalReminderMessageVo> getDayReminderMessageVoList(Long messageId, Integer day);
    
    /** 
    * @Description: 获取历史实时动态消息 
    * @Param: [paramVo] 
    * @return: java.util.List<codedriver.framework.reminder.dto.GlobalReminderMessageVo>  
    */ 
    List<GlobalReminderMessageVo> getReminderHistoryMessageList(ReminderHistoryParamVo paramVo);

    /** 
    * @Description: 获取参数天用户消息总和
    * @Param: [day] 
    * @return: int  
    */ 
    int getReminderMessageCountByDay(int day);

    /** 
    * @Description: 定时获取消息集合 
    * @return: java.util.List<com.techsure.balantflow.dto.globalreminder.GlobalReminderMessageVo>  
    */ 
    List<GlobalReminderMessageVo> getScheduleMessageList();

    /** 
    * @Description: 重置用户当天消息有效性 
    * @Param: [day] 
    * @return: void  
    */ 
    void updateDayMessageActive(int day);

    /** 
    * @Description: 订阅设置开关 
    * @Param: [reminderSubscribeVo] 
    * @return: void  
    */ 
    void updateReminderSubscribe(GlobalReminderSubscribeVo reminderSubscribeVo);
}
