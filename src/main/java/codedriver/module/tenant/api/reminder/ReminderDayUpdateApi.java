package codedriver.module.tenant.api.reminder;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.reminder.dao.mapper.GlobalReminderMessageMapper;
import codedriver.framework.reminder.dto.ReminderMessageSearchVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.reminder.GlobalReminderService;

/**
 * @program: codedriver
 * @description:
 * @create: 2019-12-10 11:55
 **/
@Service
public class ReminderDayUpdateApi extends ApiComponentBase {

	@Autowired
	private GlobalReminderMessageMapper reminderMessageMapper;
	
	@Autowired
	private GlobalReminderService reminderService;

    @Override
    public String getToken() {
        return "globalReminder/updateDayActive";
    }

    @Override
    public String getName() {
        return "重置当天消息有效性接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param(name = "day", type = ApiParamType.INTEGER, desc = "天数", isRequired = true)})
    @Description(desc = "重置当天消息有效性接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        int day = jsonObj.getInteger("day");
        Map<String, String> timeMap = reminderService.getTimeMap(day);
        ReminderMessageSearchVo searchVo = new ReminderMessageSearchVo();
        searchVo.setUserUuid(UserContext.get().getUserUuid(true));
        searchVo.setStartTime(timeMap.get("startTime"));
        searchVo.setEndTime(timeMap.get("endTime"));
        reminderMessageMapper.updateDayMessageActive(searchVo);
        return new JSONObject();
    }
}
