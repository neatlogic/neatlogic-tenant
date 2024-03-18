/*Copyright (C) 2023  深圳极向量科技有限公司 All Rights Reserved.

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

package neatlogic.module.tenant.service;

import neatlogic.framework.worktime.dao.mapper.WorktimeMapper;
import neatlogic.framework.worktime.dto.WorktimeRangeVo;
import neatlogic.framework.worktime.dto.WorktimeVo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class WorktimeServiceImpl implements WorktimeService {

    @Resource
    private WorktimeMapper worktimeMapper;

    @Override
    public void saveWorktimeRange(WorktimeVo worktimeVo, Integer year, List<String> dateList) {
        String worktimeUuid = worktimeVo.getUuid();
        JSONObject config = JSON.parseObject(worktimeVo.getConfig());
        WorktimeRangeVo worktimeRangeVo = new WorktimeRangeVo();
        worktimeRangeVo.setWorktimeUuid(worktimeUuid);
        worktimeRangeVo.setYear(year);
        worktimeMapper.deleteWorktimeRange(worktimeRangeVo);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        WorktimeRangeVo worktimeRange = null;
        JSONArray defineList = null;
        List<WorktimeRangeVo> worktimeRangeList = new ArrayList<>();
        for(String workDate : dateList) {
            LocalDate localDate= LocalDate.from(dateFormatter.parse(workDate));
            defineList = config.getJSONArray(localDate.getDayOfWeek().name().toLowerCase());
            if(defineList == null) {
                continue;
            }

            for(int i = 0; i < defineList.size(); i++) {
                JSONObject define = defineList.getJSONObject(i);
                worktimeRange = new WorktimeRangeVo();
                worktimeRange.setWorktimeUuid(worktimeUuid);
                worktimeRange.setYear(worktimeRangeVo.getYear());
                worktimeRange.setDate(workDate);
                LocalDateTime startLocalDateTime = LocalDateTime.from(dateTimeFormatter.parse(workDate + " " + define.getString("startTime")));
                LocalDateTime endLocalDateTime = LocalDateTime.from(dateTimeFormatter.parse(workDate + " " + define.getString("endTime")));
                long startTime = startLocalDateTime.toInstant(OffsetDateTime.now().getOffset()).toEpochMilli();
                long endTime = endLocalDateTime.toInstant(OffsetDateTime.now().getOffset()).toEpochMilli();
                worktimeRange.setStartTime(startTime);
                worktimeRange.setEndTime(endTime);
                worktimeRangeList.add(worktimeRange);
                if(worktimeRangeList.size() > 1000) {
                    worktimeMapper.insertBatchWorktimeRange(worktimeRangeList);
                    worktimeRangeList.clear();
                }
            }
        }
        if(worktimeRangeList.size() > 0) {
            worktimeMapper.insertBatchWorktimeRange(worktimeRangeList);
        }
    }
}
