/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.service;

import codedriver.framework.worktime.dto.WorktimeVo;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;

import java.util.List;

public interface WorktimeService {

    void saveWorktimeRange(WorktimeVo worktimeVo, Integer year, List<String> dateList);
}
