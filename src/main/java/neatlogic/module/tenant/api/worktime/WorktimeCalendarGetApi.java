/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.worktime;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.worktime.dao.mapper.WorktimeMapper;
import neatlogic.framework.worktime.dto.WorktimeRangeVo;
import neatlogic.framework.worktime.exception.WorktimeNotFoundException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class WorktimeCalendarGetApi extends PrivateApiComponentBase {

    @Resource
    private WorktimeMapper worktimeMapper;

    @Override
    public String getToken() {
        return "worktime/calendar/get";
    }

    @Override
    public String getName() {
        return "工作日历信息获取接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "worktimeUuid", type = ApiParamType.STRING, isRequired = true, desc = "工作时间窗口uuid"),
            @Param(name = "year", type = ApiParamType.INTEGER, isRequired = true, desc = "年份")
    })
    @Output({
            @Param(name = "Return", type = ApiParamType.JSONARRAY, desc = "工作日历列表")
    })
    @Description(desc = "工作日历信息获取接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        WorktimeRangeVo worktimeDetailVo = JSON.toJavaObject(jsonObj, WorktimeRangeVo.class);
        if (worktimeMapper.checkWorktimeIsExists(worktimeDetailVo.getWorktimeUuid()) == 0) {
            throw new WorktimeNotFoundException(worktimeDetailVo.getWorktimeUuid());
        }
        List<String> worktimeDateList = worktimeMapper.getWorktimeDateList(worktimeDetailVo);
        JSONArray jsonArray = generateCalendar(jsonObj.getIntValue("year"), worktimeDateList);
        return jsonArray;
    }

    /**
     * @param year             年份
     * @param worktimeDateList 已选中的日期数据
     * @return JSONArray
     * @Time:2020年8月17日
     * @Description: 生产日历数据
     */
    private JSONArray generateCalendar(int year, List<String> worktimeDateList) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        JSONObject monthObj = new JSONObject();
        JSONObject trObj = new JSONObject();
        JSONArray dateList = new JSONArray();

        JSONArray monthList = new JSONArray();
        JSONArray trList = new JSONArray();
        int trIndex = 0;
        int monthIndex = 0;
        String dateName = null;
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, 0, 1);
        for (int i = 1; i < calendar.get(Calendar.DAY_OF_WEEK); i++) {
            JSONObject nonDateObj = new JSONObject();
            nonDateObj.put("name", "");
            nonDateObj.put("dayOfMonth", "");
            nonDateObj.put("dayOfWeek", -1);
            nonDateObj.put("selected", 0);
            dateList.add(nonDateObj);
        }
        while (calendar.get(Calendar.YEAR) == year) {
            if (calendar.get(Calendar.MONTH) != monthIndex) {
                monthObj.put("name", getMonthName(monthIndex));
                monthObj.put("dateList", dateList);
                monthList.add(monthObj);
                monthIndex = calendar.get(Calendar.MONTH);
                monthObj = new JSONObject();
                dateList = new JSONArray();

                for (int i = 1; i < calendar.get(Calendar.DAY_OF_WEEK); i++) {
                    JSONObject nonDateObj = new JSONObject();
                    nonDateObj.put("name", "");
                    nonDateObj.put("dayOfMonth", "");
                    nonDateObj.put("dayOfWeek", -1);
                    nonDateObj.put("selected", 0);
                    dateList.add(nonDateObj);
                }
            }

            int index = calendar.get(Calendar.MONTH) / 4;
            if (index != trIndex) {
                trObj.put("index", trIndex);
                trObj.put("monthList", monthList);
                trList.add(trObj);
                trIndex = index;
                trObj = new JSONObject();
                monthList = new JSONArray();
            }
            JSONObject dateObj = new JSONObject();
            dateName = sdf.format(calendar.getTime());
            dateObj.put("name", dateName);
            dateObj.put("dayOfMonth", calendar.get(Calendar.DAY_OF_MONTH));
            dateObj.put("dayOfWeek", calendar.get(Calendar.DAY_OF_WEEK) - 1);
            dateObj.put("selected", worktimeDateList.contains(dateName) ? 1 : 0);
            dateList.add(dateObj);
            calendar.add(Calendar.DATE, 1);
        }

        monthObj.put("name", getMonthName(monthIndex));
        monthObj.put("dateList", dateList);
        monthList.add(monthObj);
        trObj.put("index", trIndex);
        trObj.put("monthList", monthList);
        trList.add(trObj);
        return trList;
    }

    /**
     * @param monthIndex
     * @return String
     * @Time:2020年8月17日
     * @Description: 获取月份名称
     */
    private String getMonthName(int monthIndex) {
        monthIndex += 1;
        if (monthIndex < 10) {
            return "0" + monthIndex;
        }
        return "" + monthIndex;
    }
}
