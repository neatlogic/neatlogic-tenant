/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.tenant.api.scheduler.source;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.SCHEDULE_JOB_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.scheduler.dao.mapper.SchedulerMapper;
import neatlogic.framework.scheduler.dto.ScheduleJobSourceSearchVo;
import neatlogic.framework.scheduler.dto.ScheduleJobSourceVo;
import neatlogic.framework.util.TableResultUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 查询定时作业来源管理列表。
 */
@Service
@AuthAction(action = SCHEDULE_JOB_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class JobSourceSearchApi extends PrivateApiComponentBase {

    @Resource
    private SchedulerMapper schedulerMapper;

    @Override
    public String getToken() {
        return "job/source/search";
    }

    @Override
    public String getName() {
        return "查询定时作业来源列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页码"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条数"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "名称、作业模块、服务器ID或服务器组关键字")
    })
    @Output({
            @Param(name = "tbodyList", explode = ScheduleJobSourceVo[].class, desc = "作业来源列表"),
            @Param(name = "serverGroupList", type = ApiParamType.JSONARRAY, desc = "已有服务器组列表")
    })
    @Description(desc = "查询定时作业来源列表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ScheduleJobSourceSearchVo searchVo = JSONObject.toJavaObject(jsonObj, ScheduleJobSourceSearchVo.class);
        int rowNum = schedulerMapper.searchJobSourceCount(searchVo);

//        searchVo.setPageCount(PageUtil.getPageCount(rowNum, searchVo.getPageSize()));

        // 没有数据时跳过分页SQL，降低空列表查询开销。
        List<ScheduleJobSourceVo> jobSourceList = new ArrayList<>();
        if (rowNum > 0) {
            searchVo.setRowNum(rowNum);
            jobSourceList = schedulerMapper.searchJobSource(searchVo);
        }
        JSONObject resultObj = TableResultUtil.getResult(jobSourceList, searchVo);
//        resultObj.put("serverGroupList", schedulerMapper.getJobSourceServerGroupList());
        return resultObj;
    }
}
