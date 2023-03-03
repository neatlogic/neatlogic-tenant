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

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dependency.constvalue.FrameworkFromType;
import neatlogic.framework.dependency.core.DependencyManager;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.worktime.dao.mapper.WorktimeMapper;
import neatlogic.framework.worktime.dto.WorktimeVo;
import neatlogic.framework.worktime.exception.WorktimeNotFoundException;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class WorktimeGetApi extends PrivateApiComponentBase {

    @Resource
    private WorktimeMapper worktimeMapper;

    @Override
    public String getToken() {
        return "worktime/get";
    }

    @Override
    public String getName() {
        return "工作时间窗口信息查询接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "工作时间窗口uuid")
    })
    @Output({
            @Param(explode = WorktimeVo.class)
    })
    @Description(desc = "工作时间窗口信息查询接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String uuid = jsonObj.getString("uuid");
        WorktimeVo worktime = worktimeMapper.getWorktimeByUuid(uuid);
        if (worktime == null) {
            throw new WorktimeNotFoundException(uuid);
        }
        int count = DependencyManager.getDependencyCount(FrameworkFromType.WORKTIME, uuid);
        worktime.setReferenceCount(count);
        return worktime;
    }

}
