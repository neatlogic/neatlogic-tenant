package codedriver.module.tenant.service.reminder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.dto.ModuleVo;
import codedriver.framework.reminder.core.GlobalReminderHandlerFactory;
import codedriver.framework.reminder.dto.GlobalReminderHandlerVo;
import codedriver.framework.reminder.dto.GlobalReminderMessageVo;

/**
 * @program: balantflow
 * @description:
 * @create: 2019-09-10 20:03
 **/
@Service
public class GlobalReminderServiceImpl implements GlobalReminderService {

	@Override
    public Map<String, String> getTimeMap(int day){
        Map<String, String> timeMap = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 1);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - day);
        String endTime = format.format(calendar.getTime());
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 1);
        String startTime = format.format(calendar.getTime());
        timeMap.put("startTime", startTime);
        timeMap.put("endTime", endTime);
        return timeMap;
    }

	@Override
    public void packageData(GlobalReminderMessageVo messageVo){
        GlobalReminderHandlerVo reminderVo = GlobalReminderHandlerFactory.getReminderVoMap().get(messageVo.getHandler());
        ModuleVo moduleVo = TenantContext.get().getActiveModuleMap().get(reminderVo.getModuleId());
        reminderVo.setModuleDesc(moduleVo.getDescription());
        reminderVo.setModuleName(moduleVo.getName());
        messageVo.setReminderVo(reminderVo);
    }
}
