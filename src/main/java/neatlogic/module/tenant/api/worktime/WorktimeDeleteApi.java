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

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.WORKTIME_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dependency.constvalue.FrameworkFromType;
import neatlogic.framework.dependency.core.DependencyManager;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.worktime.dao.mapper.WorktimeMapper;
import neatlogic.framework.worktime.dto.WorktimeRangeVo;
import neatlogic.framework.worktime.dto.WorktimeVo;
import neatlogic.framework.worktime.exception.WorktimeHasBeenRelatedByChannelException;
import neatlogic.framework.worktime.exception.WorktimeNotFoundException;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
@AuthAction(action = WORKTIME_MODIFY.class)
public class WorktimeDeleteApi extends PrivateApiComponentBase {

    @Resource
    private WorktimeMapper worktimeMapper;

    @Override
    public String getToken() {
        return "worktime/delete";
    }

    @Override
    public String getName() {
        return "工作时间窗口删除接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "工作时间窗口uuid")
    })
    @Description(desc = "工作时间窗口删除接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        /**
         * 如果被服务引用，则不能删除
         * 如果没被服务引用，却被工单引用，则逻辑删除
         */
        String uuid = jsonObj.getString("uuid");
        if (worktimeMapper.checkWorktimeIsExists(uuid) == 0) {
            throw new WorktimeNotFoundException(uuid);
        }
        WorktimeVo worktime = worktimeMapper.getWorktimeByUuid(uuid);
        if (DependencyManager.getDependencyCount(FrameworkFromType.WORKTIME, uuid) > 0) {
            throw new WorktimeHasBeenRelatedByChannelException(worktime.getName());
        }
        if (DependencyManager.getDependencyCount(FrameworkFromType.WORKTIME, uuid, false) > 0) {
            worktime.setIsDelete(1);
            worktime.setLcu(UserContext.get().getUserUuid());
            worktimeMapper.updateWorktimeDeleteStatus(worktime);
        } else {
            worktimeMapper.deleteWorktimeByUuid(uuid);
            WorktimeRangeVo worktimeRangeVo = new WorktimeRangeVo();
            worktimeRangeVo.setWorktimeUuid(uuid);
            worktimeMapper.deleteWorktimeRange(worktimeRangeVo);
        }

        return null;
    }

}
