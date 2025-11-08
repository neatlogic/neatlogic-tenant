/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.api.systemnotice;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.SYSTEM_NOTICE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.scheduler.core.IJob;
import neatlogic.framework.scheduler.core.SchedulerManager;
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.framework.scheduler.exception.ScheduleHandlerNotFoundException;
import neatlogic.framework.systemnotice.dao.mapper.SystemNoticeMapper;
import neatlogic.framework.systemnotice.dto.SystemNoticeVo;
import neatlogic.framework.systemnotice.exception.SystemNoticeNotFoundException;
import neatlogic.module.framework.systemnotice.schedule.StopSystemNoticeJob;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@AuthAction(action = SYSTEM_NOTICE_MODIFY.class)
@Service
@OperationType(type = OperationTypeEnum.OPERATE)
public class SystemNoticeStopApi extends PrivateApiComponentBase {

    @Resource
    private SystemNoticeMapper systemNoticeMapper;

    @Resource
    private SchedulerManager schedulerManager;

    @Override
    public String getToken() {
        return "systemnotice/stop";
    }

    @Override
    public String getName() {
        return "停用系统公告";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "公告ID")})
    @Output({})
    @Description(desc = "停用系统公告")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        SystemNoticeVo vo = systemNoticeMapper.getSystemNoticeBaseInfoById(id);
        if(vo == null){
            throw new SystemNoticeNotFoundException(id);
        }
        vo.setLcu(UserContext.get().getUserUuid());
        /** 停用后清除下发时段，下次下发时再指定 **/
        systemNoticeMapper.stopSystemNoticeById(vo);

        IJob jobHandler = SchedulerManager.getHandler(StopSystemNoticeJob.class.getName());
        if (jobHandler == null) {
            throw new ScheduleHandlerNotFoundException(StopSystemNoticeJob.class.getName());
        }
        String tenantUuid = TenantContext.get().getTenantUuid();
        JobObject jobObject = new JobObject.Builder(vo.getId().toString(), jobHandler.getGroupName(), jobHandler.getClassName(), tenantUuid)
                .build();
        schedulerManager.unloadJob(jobObject);

        return null;
    }
}
