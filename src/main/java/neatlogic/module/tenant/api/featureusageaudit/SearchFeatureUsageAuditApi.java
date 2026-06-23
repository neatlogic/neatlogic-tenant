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
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.ModuleUtil;
import neatlogic.framework.dao.mapper.FeatureUsageAuditMapper;
import neatlogic.framework.dto.featureusageaudit.FeatureUsageAuditSearchVo;
import neatlogic.framework.dto.featureusageaudit.FeatureUsageAuditVo;
import neatlogic.framework.dto.module.ModuleGroupVo;
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
public class SearchFeatureUsageAuditApi extends PrivateApiComponentBase {

    @Resource
    private FeatureUsageAuditMapper featureUsageAuditMapper;

    @Override
    public String getToken() {
        return "feature/usage/audit/search";
    }

    @Override
    public String getName() {
        return "nmtaf.searchfeatureusageauditapi.getname";
    }

    @Input({
            @Param(name = "loginAuditId", type = ApiParamType.LONG, desc = "common.id"),
            @Param(name = "moduleGroupList", type = ApiParamType.JSONARRAY, desc = "common.modulegroup"),
            @Param(name = "featureNameList", type = ApiParamType.JSONARRAY, desc = "common.featurename"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize"),
            @Param(name = "timeRange", type = ApiParamType.INTEGER, desc = "common.timerange"),
            @Param(name = "timeUnit", type = ApiParamType.STRING, desc = "common.timeunit"),
            @Param(name = "startTime", type = ApiParamType.LONG, desc = "common.starttime"),
            @Param(name = "endTime", type = ApiParamType.LONG, desc = "common.endtime"),
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "tbodyList", explode = FeatureUsageAuditVo[].class, desc = "common.tbodylist")
    })
    @Description(desc = "nmtaf.searchfeatureusageauditapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        FeatureUsageAuditSearchVo searchVo = paramObj.toJavaObject(FeatureUsageAuditSearchVo.class);
        String userUuid = paramObj.getString("userUuid");
        if (StringUtils.isNotBlank(userUuid) && userUuid.startsWith(GroupSearch.USER.getValuePlugin())) {
            searchVo.setUserUuid(GroupSearch.removePrefix(userUuid));
        }
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
        int rowNum = featureUsageAuditMapper.getFeatureUsageAuditCount(searchVo);
        if (rowNum > 0) {
            searchVo.setRowNum(rowNum);
            tbodyList = featureUsageAuditMapper.getFeatureUsageAuditList(searchVo);
            handleTbodyList(tbodyList);
        }
        return TableResultUtil.getResult(tbodyList, searchVo);
    }

    private void handleTbodyList(List<FeatureUsageAuditVo> tbodyList) {
        for (FeatureUsageAuditVo featureUsageAuditVo : tbodyList) {
            ModuleGroupVo groupVo = ModuleUtil.getModuleGroup(featureUsageAuditVo.getModuleGroup());
            if (groupVo != null) {
                featureUsageAuditVo.setModuleGroupName(groupVo.getGroupName());
            } else {
                featureUsageAuditVo.setModuleGroupName(featureUsageAuditVo.getModuleGroup());
            }
        }
    }
}
