/*
Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package neatlogic.module.tenant.api.worktime;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.WORKTIME_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.sla.core.SlaRecalculateManager;
import neatlogic.framework.transaction.core.EscapeTransactionJob;
import neatlogic.framework.util.RegexUtils;
import neatlogic.framework.worktime.dao.mapper.WorktimeMapper;
import neatlogic.framework.worktime.dto.WorktimeRangeVo;
import neatlogic.framework.worktime.dto.WorktimeVo;
import neatlogic.framework.worktime.exception.WorktimeConfigIllegalException;
import neatlogic.framework.worktime.exception.WorktimeNameRepeatException;
import neatlogic.framework.worktime.exception.WorktimeStartTimeGreaterThanOrEqualToEndTimeException;
import neatlogic.module.tenant.service.WorktimeService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map.Entry;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = WORKTIME_MODIFY.class)
public class WorktimeSaveApi extends PrivateApiComponentBase {

    @Resource
    private WorktimeMapper worktimeMapper;

    @Resource
    private WorktimeService worktimeService;

    @Override
    public String getToken() {
        return "worktime/save";
    }

    @Override
    public String getName() {
        return "工作时间窗口信息保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "工作时间窗口uuid"),
            @Param(name = "name", type = ApiParamType.REGEX, rule = RegexUtils.NAME, isRequired = true, maxLength = 50, desc = "工作时间窗口名称"),
            @Param(name = "isActive", type = ApiParamType.ENUM, isRequired = true, desc = "是否激活", rule = "0,1"),
            @Param(name = "config", type = ApiParamType.JSONOBJECT, isRequired = true, desc = "每周工作时段的定义")
    })
    @Output({
            @Param(name = "Return", type = ApiParamType.STRING, desc = "工作时间窗口uuid")
    })
    @Description(desc = "工作时间窗口信息保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        WorktimeVo worktimeVo = JSONObject.toJavaObject(jsonObj, WorktimeVo.class);
        if (worktimeMapper.checkWorktimeNameIsRepeat(worktimeVo) > 0) {
            throw new WorktimeNameRepeatException(worktimeVo.getName());
        }
        //验证config
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");
        JSONObject configJson = jsonObj.getJSONObject("config");
        for (Entry<String, Object> entry : configJson.entrySet()) {
            try {
                DayOfWeek.valueOf(entry.getKey().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new WorktimeConfigIllegalException(entry.getKey());
            }
            Object value = entry.getValue();
            if (value instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) value;
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String startTime = obj.getString("startTime");
                    if (StringUtils.isBlank(startTime)) {
                        throw new WorktimeConfigIllegalException("startTime");
                    }
                    try {
                        timeFormatter.parse(startTime);
                    } catch (DateTimeException e) {
                        throw new WorktimeConfigIllegalException(startTime);
                    }
                    String endTime = obj.getString("endTime");
                    if (StringUtils.isBlank(endTime)) {
                        throw new WorktimeConfigIllegalException("endTime");
                    }
                    try {
                        timeFormatter.parse(endTime);
                    } catch (DateTimeException e) {
                        throw new WorktimeConfigIllegalException(endTime);
                    }
                    if (startTime.compareTo(endTime) > 0) {
                        throw new WorktimeStartTimeGreaterThanOrEqualToEndTimeException();
                    }
                }
            } else {
                throw new WorktimeConfigIllegalException(value.toString());
            }
        }

        worktimeVo.setLcu(UserContext.get().getUserUuid(true));
        String uuid = worktimeVo.getUuid();
        if (worktimeMapper.checkWorktimeIsExists(uuid) == 0) {
            worktimeVo.setUuid(null);
            worktimeMapper.insertWorktime(worktimeVo);
            uuid = worktimeVo.getUuid();
        } else {
            worktimeMapper.updateWorktime(worktimeVo);
        }

        List<Integer> yearList = worktimeMapper.getYearListByWorktimeUuid(uuid);
        WorktimeRangeVo worktimeRangeVo = new WorktimeRangeVo();
        worktimeRangeVo.setWorktimeUuid(uuid);
        for (Integer year : yearList) {
            worktimeRangeVo.setYear(year);
            List<String> dateList = worktimeMapper.getWorktimeDateList(worktimeRangeVo);
            worktimeService.saveWorktimeRange(worktimeVo, year, dateList);
        }
        // 当服务窗口排班更新时，对与该服务窗口相关的未完成SLA进行耗时重算
        final String worktimeUuid = uuid;
        EscapeTransactionJob.State s = new EscapeTransactionJob(() -> {
            SlaRecalculateManager.execute(worktimeUuid);
        }).execute();

        return uuid;
    }

    public IValid name() {
        return value -> {
            WorktimeVo worktimeVo = JSONObject.toJavaObject(value, WorktimeVo.class);
            if (worktimeMapper.checkWorktimeNameIsRepeat(worktimeVo) > 0) {
                return new FieldValidResultVo(new WorktimeNameRepeatException(worktimeVo.getName()));
            }
            return new FieldValidResultVo();
        };
    }
}
