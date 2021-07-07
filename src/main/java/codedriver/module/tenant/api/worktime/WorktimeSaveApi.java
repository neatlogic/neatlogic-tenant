/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.worktime;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.WORKTIME_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.FieldValidResultVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.IValid;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.worktime.dao.mapper.WorktimeMapper;
import codedriver.framework.worktime.dto.WorktimeVo;
import codedriver.framework.worktime.exception.WorktimeConfigIllegalException;
import codedriver.framework.worktime.exception.WorktimeNameRepeatException;
import codedriver.framework.worktime.exception.WorktimeStartTimeGreaterThanOrEqualToEndTimeException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = WORKTIME_MODIFY.class)
public class WorktimeSaveApi extends PrivateApiComponentBase {

    @Resource
    private WorktimeMapper worktimeMapper;

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
            @Param(name = "name", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", isRequired = true, maxLength = 50, desc = "工作时间窗口名称"),
            @Param(name = "isActive", type = ApiParamType.ENUM, isRequired = true, desc = "是否激活", rule = "0,1"),
            @Param(name = "config", type = ApiParamType.JSONOBJECT, isRequired = true, desc = "每周工作时段的定义")
    })
    @Output({
            @Param(name = "Return", type = ApiParamType.STRING, desc = "工作时间窗口uuid")
    })
    @Description(desc = "工作时间窗口信息保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        WorktimeVo worktimeVo = JSON.toJavaObject(jsonObj, WorktimeVo.class);
        if (worktimeMapper.checkWorktimeNameIsRepeat(worktimeVo) > 0) {
            throw new WorktimeNameRepeatException(worktimeVo.getName());
        }
        //验证config
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");
        JSONObject configJson = JSON.parseObject(worktimeVo.getConfig());
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

        return uuid;
    }

    public IValid name() {
        return value -> {
            WorktimeVo worktimeVo = JSON.toJavaObject(value, WorktimeVo.class);
            if (worktimeMapper.checkWorktimeNameIsRepeat(worktimeVo) > 0) {
                return new FieldValidResultVo(new WorktimeNameRepeatException(worktimeVo.getName()));
            }
            return new FieldValidResultVo();
        };
    }
}
