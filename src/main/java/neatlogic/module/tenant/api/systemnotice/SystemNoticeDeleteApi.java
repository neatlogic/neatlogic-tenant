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
import neatlogic.module.framework.systemnotice.schedule.IssueSystemNoticeJob;
import neatlogic.module.framework.systemnotice.schedule.StopSystemNoticeJob;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@AuthAction(action = SYSTEM_NOTICE_MODIFY.class)
@Service
@OperationType(type = OperationTypeEnum.DELETE)
@Transactional
public class SystemNoticeDeleteApi extends PrivateApiComponentBase {

    @Resource
    private SystemNoticeMapper systemNoticeMapper;

    @Resource
    private SchedulerManager schedulerManager;

    @Override
    public String getToken() {
        return "systemnotice/delete";
    }

    @Override
    public String getName() {
        return "删除系统公告";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "公告ID")})
    @Output({})
    @Description(desc = "删除系统公告")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        SystemNoticeVo vo = systemNoticeMapper.getSystemNoticeBaseInfoById(jsonObj.getLong("id"));
        if(vo == null){
            throw new SystemNoticeNotFoundException(jsonObj.getLong("id"));
        }
        /** 不再限制删除已下发公告 **/
//        if(SystemNoticeVo.Status.ISSUED.getValue().equals(vo.getStatus())){
//            throw new SystemNoticeHasBeenIssuedException(vo.getTitle());
//        }
        /** 只删除system_notice与system_notice_recipient，
         * system_notice_user由每个用户登录或者pull时自我删除
         **/
        systemNoticeMapper.deleteRecipientByNoticeId(vo.getId());
        systemNoticeMapper.deleteSystemNoticeById(vo.getId());
        systemNoticeMapper.deleteSystemNoticeUserByNoticeId(vo.getId());
        {
            IJob jobHandler = SchedulerManager.getHandler(IssueSystemNoticeJob.class.getName());
            if (jobHandler == null) {
                throw new ScheduleHandlerNotFoundException(IssueSystemNoticeJob.class.getName());
            }
            String tenantUuid = TenantContext.get().getTenantUuid();
            JobObject jobObject = new JobObject.Builder(vo.getId().toString(), jobHandler.getGroupName(), jobHandler.getClassName(), tenantUuid)
                    .build();
            schedulerManager.unloadJob(jobObject);
            schedulerManager.deleteJobSource(jobObject);
        }
        {
            IJob jobHandler = SchedulerManager.getHandler(StopSystemNoticeJob.class.getName());
            if (jobHandler == null) {
                throw new ScheduleHandlerNotFoundException(StopSystemNoticeJob.class.getName());
            }
            String tenantUuid = TenantContext.get().getTenantUuid();
            JobObject jobObject = new JobObject.Builder(vo.getId().toString(), jobHandler.getGroupName(), jobHandler.getClassName(), tenantUuid)
                    .build();
            schedulerManager.unloadJob(jobObject);
            schedulerManager.deleteJobSource(jobObject);
        }
        return null;
    }
}
