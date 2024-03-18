/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tenant.api.worktime;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.WORKTIME_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.sla.core.SlaRecalculateManager;
import neatlogic.framework.transaction.core.EscapeTransactionJob;
import neatlogic.framework.worktime.dao.mapper.WorktimeMapper;
import neatlogic.framework.worktime.dto.WorktimeVo;
import neatlogic.framework.worktime.exception.WorktimeNotFoundException;
import neatlogic.module.tenant.service.WorktimeService;
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
		// 当服务窗口排班更新时，对与该服务窗口相关的未完成SLA进行耗时重算
		EscapeTransactionJob.State s = new EscapeTransactionJob(() -> {
			SlaRecalculateManager.execute(worktimeUuid);
		}).execute();
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
