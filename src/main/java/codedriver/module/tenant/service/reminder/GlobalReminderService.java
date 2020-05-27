package codedriver.module.tenant.service.reminder;

import java.util.Map;

import codedriver.framework.reminder.dto.GlobalReminderMessageVo;

public interface GlobalReminderService {
    
	public Map<String, String> getTimeMap(int day);
	
	public void packageData(GlobalReminderMessageVo messageVo);
    
}
