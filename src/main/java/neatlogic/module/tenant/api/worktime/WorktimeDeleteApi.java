/*
Copyright(c) $today.year NeatLogic Co., Ltd. All Rights Reserved.

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
