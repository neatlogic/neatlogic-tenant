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

package neatlogic.module.tenant.api.healthcheck;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadpool.CachedThreadPool;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.healthcheck.ThreadPoolVo;
import neatlogic.framework.dto.healthcheck.ThreadTaskVo;
import neatlogic.framework.dto.healthcheck.ThreadVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetThreadPoolStatusApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "/healthcheck/threadpool/status";
    }

    @Override
    public String getName() {
        return "获取线程池状态";
    }

    @Override
    public String getConfig() {
        return null;
    }


    @Input({
            @Param(name = "isShowCurrentTenant", type = ApiParamType.ENUM, rule = "0,1", desc = "是否只看当前租户数据")
    })
    @Description(desc = "获取线程池状态")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Integer isShowCurrentTenant = paramObj.getInteger("isShowCurrentTenant");
        ThreadPoolVo threadPoolVo = CachedThreadPool.getStatus();
        if (Objects.equals(isShowCurrentTenant, 1)) {
            String tenantUuid = TenantContext.get().getTenantUuid();
            List<Long> idList = new ArrayList<>();
            List<ThreadTaskVo> threadTaskList = threadPoolVo.getThreadTaskList();
            if (CollectionUtils.isNotEmpty(threadTaskList)) {
                for (int i = threadTaskList.size() - 1; i >= 0; i--) {
                    ThreadTaskVo threadTaskVo = threadTaskList.get(i);
                    if (Objects.equals(threadTaskVo.getTenantUuid(), tenantUuid)) {
                        idList.add(threadTaskVo.getId());
                    } else {
                        threadTaskList.remove(i);
                    }
                }
            }
            List<ThreadVo> threadList = threadPoolVo.getThreadList();
            if (CollectionUtils.isNotEmpty(threadList)) {
                for (int i = threadList.size() - 1; i >= 0; i--) {
                    ThreadVo threadVo = threadList.get(i);
                    if (!idList.contains(threadVo.getId())) {
                        threadList.remove(i);
                    }
                }
            }
        }
        return threadPoolVo;
    }


}
