/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.tenant.api.featureusageaudit;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.FeatureUsageAuditMapper;
import neatlogic.framework.dto.featureusageaudit.FeatureUsageAuditSearchVo;
import neatlogic.framework.dto.featureusageaudit.FeatureUsageAuditVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
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
public class SearchFeatureApi extends PrivateApiComponentBase {

    @Resource
    private FeatureUsageAuditMapper featureUsageAuditMapper;

    @Override
    public String getToken() {
        return "feature/search";
    }

    @Override
    public String getName() {
        return "统计功能使用次数列表";
    }

    @Input({
            @Param(name = "userUuid", type = ApiParamType.STRING, isRequired = false, desc = "用户UUID"),
            @Param(name = "moduleGroup", type = ApiParamType.STRING, isRequired = false, desc = "模块组"),
            @Param(name = "menuPath", type = ApiParamType.STRING, desc = "menu path"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize"),
            @Param(name = "timeRange", type = ApiParamType.INTEGER, desc = "common.duration"),
            @Param(name = "timeUnit", type = ApiParamType.STRING, desc = "common.timeunit"),
            @Param(name = "startTime", type = ApiParamType.LONG, isRequired = true, desc = "common.starttime"),
            @Param(name = "endTime", type = ApiParamType.LONG, isRequired = true, desc = "common.endtime"),
    })
    @Output({
            @Param(name = "tbodyList", explode = FeatureUsageAuditVo[].class, desc = "common.tbodylist")
    })
    @Description(desc = "统计功能使用次数列表")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        FeatureUsageAuditSearchVo searchVo = paramObj.toJavaObject(FeatureUsageAuditSearchVo.class);
        //将时间范围转为 开始时间、结束时间
        if (searchVo.getStartTime() == null && searchVo.getEndTime() == null) {
            Integer timeRange = paramObj.getInteger("timeRange");
            String timeUnit = paramObj.getString("timeUnit");
            if (timeRange != null && StringUtils.isNotBlank(timeUnit)) {
                searchVo.setStartTime(TimeUtil.recentTimeTransfer(timeRange, timeUnit));
                searchVo.setEndTime(new Date());
            }
        }
        List<FeatureUsageAuditVo> tbodyList = new ArrayList<>();
        int rowNum = featureUsageAuditMapper.getFeatureCount(searchVo);
        if (rowNum > 0) {
            searchVo.setRowNum(rowNum);
            tbodyList = featureUsageAuditMapper.getFeatureList(searchVo);
        }
        return TableResultUtil.getResult(tbodyList, searchVo);
    }
}
