/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.worktime;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.WORKTIME_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.worktime.dao.mapper.WorktimeMapper;
import codedriver.framework.worktime.dto.WorktimeVo;
import codedriver.framework.worktime.exception.WorktimeNotFoundException;
import codedriver.module.tenant.service.WorktimeService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = WORKTIME_MODIFY.class)
public class WorktimeCalendarSaveApi extends PrivateApiComponentBase {

	@Resource
	private WorktimeMapper worktimeMapper;

	@Resource
	private WorktimeService worktimeService;
	
	@Override
	public String getToken() {
		return "worktime/calendar/save";
	}

	@Override
	public String getName() {
		return "工作日历信息保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "worktimeUuid", type = ApiParamType.STRING, isRequired = true, desc = "工作时间窗口uuid"),
		@Param(name = "year", type = ApiParamType.INTEGER, isRequired = true, desc = "年份"),
		@Param(name = "calendarList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "工作日历列表")
	})
	@Description(desc = "工作日历信息保存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String worktimeUuid = jsonObj.getString("worktimeUuid");
		WorktimeVo worktimeVo = worktimeMapper.getWorktimeByUuid(worktimeUuid);
		if(worktimeVo == null) {
			throw new WorktimeNotFoundException(worktimeUuid);
		}

		Integer year = jsonObj.getInteger("year");
		JSONArray calendarList = jsonObj.getJSONArray("calendarList");
		worktimeService.saveWorktimeRange(worktimeVo, year, generateDateList(calendarList));
		return null;
	}

	private List<String> generateDateList(JSONArray calendarList) {
		List<String> resultList = new ArrayList<>();
	
		if(calendarList == null || calendarList.isEmpty()) {
			return resultList;
		}
		for(int i = 0; i < calendarList.size(); i++) {
			JSONObject trObj = calendarList.getJSONObject(i);
			if(trObj == null || trObj.isEmpty()) {
				continue;
			}
			JSONArray monthList = trObj.getJSONArray("monthList");
			if(monthList == null || monthList.isEmpty()) {
				continue;
			}
			for(int j = 0; j < monthList.size(); j++) {
				JSONObject monthObj = monthList.getJSONObject(j);
				if(monthObj == null || monthObj.isEmpty()) {
					continue;
				}
				JSONArray dateList = monthObj.getJSONArray("dateList");
				if(dateList == null || dateList.isEmpty()) {
					continue;
				}
				for(int k = 0; k < dateList.size(); k++) {
					JSONObject dateObj = dateList.getJSONObject(k);
					if(dateObj == null || dateObj.isEmpty()) {
						continue;
					}

					int selected = dateObj.getIntValue("selected");
					if(selected == 1) {//被选中的日期
						resultList.add(dateObj.getString("name"));
					}
				}
			}
		}
		return resultList;
	}
}