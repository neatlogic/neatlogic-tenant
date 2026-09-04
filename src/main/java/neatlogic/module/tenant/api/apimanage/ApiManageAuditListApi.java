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

package neatlogic.module.tenant.api.apimanage;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.systemuser.SystemUserFactory;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.exception.type.ApiNotFoundException;
import neatlogic.framework.exception.util.StartTimeAndEndTimeCanNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentFactory;
import neatlogic.framework.restful.dao.mapper.ApiAuditMapper;
import neatlogic.framework.restful.dao.mapper.ApiMapper;
import neatlogic.framework.restful.dto.ApiAuditVo;
import neatlogic.framework.restful.dto.ApiVo;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.framework.util.TimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ApiManageAuditListApi extends PrivateApiComponentBase {

    @Resource
    private ApiMapper ApiMapper;

    @Resource
    private ApiAuditMapper apiAuditMapper;

    @Override
    public String getToken() {
        return "apimanage/audit/list";
    }

    @Override
    public String getName() {
        return "nmtaa.apimanageauditlistapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "token", type = ApiParamType.STRING, isRequired = true, desc = "nmtaa.apimanageauditlistapi.input.param.desc.token"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage", help = "nmtaa.apimanageauditlistapi.input.param.help.currentpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize", help = "nmtaa.apimanageauditlistapi.input.param.help.pagesize"),
            @Param(name = "startTime", type = ApiParamType.LONG, desc = "common.starttime"),
            @Param(name = "endTime", type = ApiParamType.LONG, desc = "common.endtime"),
            @Param(name = "timeRange", type = ApiParamType.INTEGER, desc = "common.timerange"),
            @Param(name = "timeUnit", type = ApiParamType.ENUM, rule = "year,month,week,day,hour", desc = "common.timeunit"),
            @Param(name = "userUuidList", type = ApiParamType.JSONARRAY, desc = "common.useruuidlist"),
            @Param(name = "statusList", type = ApiParamType.JSONARRAY, desc = "common.statuslist"),
            @Param(name = "type", type = ApiParamType.ENUM, rule = "rest,mcp", desc = "common.accesstype")
    })
    @Output({
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize"),
            @Param(name = "pageCount", type = ApiParamType.INTEGER, desc = "common.pagecount"),
            @Param(name = "rowNum", type = ApiParamType.INTEGER, desc = "common.rownum"),
            @Param(name = "tbodyList", explode = ApiAuditVo[].class, isRequired = true, desc = "nmtaa.apimanageauditlistapi.output.param.desc.tbodylist")
    })
    @Description(desc = "nmtaa.apimanageauditlistapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<ApiAuditVo> apiAuditList = new ArrayList<>();
        ApiAuditVo apiAuditVo = jsonObj.toJavaObject(ApiAuditVo.class);
        if (PrivateApiComponentFactory.getApiByToken(apiAuditVo.getToken()) == null) {
            ApiVo api = ApiMapper.getApiByToken(apiAuditVo.getToken());
            if (api == null) {
                throw new ApiNotFoundException(apiAuditVo.getToken());
            }
        }
        //将时间范围转为 开始时间、结束时间
        if (apiAuditVo.getStartTime() == null && apiAuditVo.getEndTime() == null) {
            Integer timeRange = jsonObj.getInteger("timeRange");
            String timeUnit = jsonObj.getString("timeUnit");
            if (timeRange != null && StringUtils.isNotBlank(timeUnit)) {
                apiAuditVo.setStartTime(TimeUtil.recentTimeTransfer(timeRange, timeUnit));
                apiAuditVo.setEndTime(new Date());
            }
        }

        if (apiAuditVo.getStartTime() == null || apiAuditVo.getEndTime() == null) {
            throw new StartTimeAndEndTimeCanNotFoundException();
        }

        int rowNum = apiAuditMapper.getApiAuditCount(apiAuditVo);
        if (rowNum > 0) {
            apiAuditVo.setRowNum(rowNum);
            apiAuditVo.setPageCount(PageUtil.getPageCount(rowNum, apiAuditVo.getPageSize()));
            apiAuditList = apiAuditMapper.getApiAuditList(apiAuditVo);
            for (ApiAuditVo apiAudit : apiAuditList) {
                if (StringUtils.isBlank(apiAudit.getUserName())) {
                    UserVo userVo = SystemUserFactory.getUserVoByUser(apiAudit.getUserUuid());
                    if (userVo != null) {
                        apiAudit.setUserName(userVo.getUserName());
                    }
                }
            }
        }
        return TableResultUtil.getResult(apiAuditList, apiAuditVo);
    }

}
