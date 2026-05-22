/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x - 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.api.healthcheck;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dao.plugin.SqlCostInterceptor;
import neatlogic.framework.dto.healthcheck.RequestSqlAuditVo;
import neatlogic.framework.dto.healthcheck.SqlAuditVo;
import neatlogic.framework.healthcheck.SqlAuditManager;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class SearchSqlAuditApi extends PrivateApiComponentBase {

    private static final int PAGE_SIZE = 20;

    @Override
    public String getToken() {
        return "/healthcheck/sqldump";
    }

    @Override
    public String getName() {
        return "获取SQL耗时";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字"),
            @Param(name = "tenant", type = ApiParamType.STRING, desc = "租户"),
            @Param(name = "userId", type = ApiParamType.STRING, desc = "用户"),
            @Param(name = "orderBy", type = ApiParamType.ENUM, rule = "timecost,runtime", desc = "排序，只支持timecost和runtime"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "SQL ID监控当前页"),
            @Param(name = "requestCurrentPage", type = ApiParamType.INTEGER, desc = "URL监控当前页")
    })
    @Output({
            @Param(name = "sqlAuditData", type = ApiParamType.JSONOBJECT, desc = "SQL ID监控数据"),
            @Param(name = "requestSqlAuditData", type = ApiParamType.JSONOBJECT, desc = "URL监控数据")
    })
    @Description(desc = "获取SQL耗时接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String orderBy = StringUtils.isNotBlank(paramObj.getString("orderBy")) ? paramObj.getString("orderBy") : "runtime";
        String keyword = paramObj.getString("keyword");
        String tenant = paramObj.getString("tenant");
        String userId = paramObj.getString("userId");
        // SQL ID监控和URL监控共用keyword、tenant、userId过滤条件，但仍分别分页避免两个Tab互相影响页码
        JSONObject sqlAuditData = buildSqlAuditData(keyword, tenant, userId, orderBy, paramObj.getIntValue("currentPage"));
        JSONObject requestSqlAuditData = buildRequestSqlAuditData(keyword, tenant, userId, orderBy, paramObj.getIntValue("requestCurrentPage"));
        JSONObject returnObj = new JSONObject();
        returnObj.put("sqlAuditData", sqlAuditData);
        returnObj.put("requestSqlAuditData", requestSqlAuditData);
        returnObj.put("maxTimeCost", sqlAuditData.getLongValue("maxTimeCost"));
        returnObj.put("maxRequestTimeCost", requestSqlAuditData.getLongValue("maxRequestTimeCost"));
        returnObj.put("sqlIdList", SqlCostInterceptor.SqlIdMap.getSqlIdList());
        returnObj.put("urlList", SqlCostInterceptor.UrlMap.getUrlList());
        return returnObj;
    }

    private JSONObject buildSqlAuditData(String keyword, String tenant, String userId, String orderBy, int currentPage) {
        // 复制新列表，避免前端分页和排序影响内存中的原始审计列表
        List<SqlAuditVo> sqlAuditList = new ArrayList<>(SqlAuditManager.getSqlAuditList());
        if (StringUtils.isNotBlank(keyword)) {
            // SQL ID表的关键字沿用原id搜索语义，统一从keyword入参取值
            sqlAuditList = sqlAuditList.stream().filter(d -> containsIgnoreCase(d.getId(), keyword)).collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(tenant)) {
            // SQL ID表按租户精确过滤，便于定位指定租户的SQL记录
            sqlAuditList = sqlAuditList.stream().filter(d -> Objects.equals(d.getTenant(), tenant)).collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(userId)) {
            // SQL ID表按用户精确过滤，便于定位指定用户触发的SQL记录
            sqlAuditList = sqlAuditList.stream().filter(d -> Objects.equals(d.getUserId(), userId)).collect(Collectors.toList());
        }
        if (Objects.equals(orderBy, "timecost")) {
            sqlAuditList = sqlAuditList.stream().sorted((o1, o2) -> o2.getTimeCost().compareTo(o1.getTimeCost())).collect(Collectors.toList());
        } else {
            sqlAuditList = sqlAuditList.stream().sorted((o1, o2) -> o2.getRunTime().compareTo(o1.getRunTime())).collect(Collectors.toList());
        }
        long maxTimeCost = 0;
        for (SqlAuditVo sqlAuditVo : sqlAuditList) {
            if (sqlAuditVo.getTimeCost() != null && maxTimeCost < sqlAuditVo.getTimeCost()) {
                maxTimeCost = sqlAuditVo.getTimeCost();
            }
        }
        JSONObject data = pageList(sqlAuditList, currentPage);
        data.put("maxTimeCost", maxTimeCost);
        return data;
    }

    private JSONObject buildRequestSqlAuditData(String keyword, String tenant, String userId, String orderBy, int currentPage) {
        // URL监控列表按一次HTTP请求一行展示，和SQL ID明细列表完全分开
        List<RequestSqlAuditVo> requestSqlAuditList = new ArrayList<>(SqlAuditManager.getRequestSqlAuditList());
        if (StringUtils.isNotBlank(keyword)) {
            // URL表的关键字沿用原url搜索语义，统一从keyword入参取值
            requestSqlAuditList = requestSqlAuditList.stream().filter(d -> containsIgnoreCase(d.getUrl(), keyword)).collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(tenant)) {
            // URL表按租户精确过滤，和SQL ID表保持一致
            requestSqlAuditList = requestSqlAuditList.stream().filter(d -> Objects.equals(d.getTenant(), tenant)).collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(userId)) {
            // URL表按用户精确过滤，和SQL ID表保持一致
            requestSqlAuditList = requestSqlAuditList.stream().filter(d -> Objects.equals(d.getUserId(), userId)).collect(Collectors.toList());
        }
        if (Objects.equals(orderBy, "timecost")) {
            requestSqlAuditList = requestSqlAuditList.stream().sorted((o1, o2) -> Long.compare(o2.getTotalTimeCost(), o1.getTotalTimeCost())).collect(Collectors.toList());
        } else {
            requestSqlAuditList = requestSqlAuditList.stream().sorted((o1, o2) -> o2.getRunTime().compareTo(o1.getRunTime())).collect(Collectors.toList());
        }
        long maxRequestTimeCost = 0;
        for (RequestSqlAuditVo requestSqlAuditVo : requestSqlAuditList) {
            if (maxRequestTimeCost < requestSqlAuditVo.getTotalTimeCost()) {
                maxRequestTimeCost = requestSqlAuditVo.getTotalTimeCost();
            }
        }
        JSONObject data = pageList(requestSqlAuditList, currentPage);
        data.put("maxRequestTimeCost", maxRequestTimeCost);
        return data;
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        // SQL监控关键字统一使用忽略大小写的包含匹配，兼容原SQL ID和URL搜索体验
        return StringUtils.isNotBlank(source) && StringUtils.isNotBlank(keyword) && source.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    private JSONObject pageList(List<?> list, int currentPage) {
        int rowNum = list.size();
        int pageCount = PageUtil.getPageCount(rowNum, PAGE_SIZE);
        // 空列表也返回第1页，避免前端表格收到currentPage=0
        pageCount = Math.max(1, pageCount);
        currentPage = Math.max(1, currentPage);
        currentPage = Math.min(currentPage, pageCount);
        List<?> tbodyList;
        try {
            // 审计数据可能在请求途中被清空，分页异常时直接返回空列表
            tbodyList = list.stream().skip((long) (currentPage - 1) * PAGE_SIZE).limit(PAGE_SIZE).collect(Collectors.toList());
        } catch (Exception ex) {
            tbodyList = new ArrayList<>();
        }
        JSONObject returnObj = new JSONObject();
        returnObj.put("pageCount", pageCount);
        returnObj.put("currentPage", currentPage);
        returnObj.put("pageSize", PAGE_SIZE);
        returnObj.put("rowNum", rowNum);
        returnObj.put("tbodyList", tbodyList);
        return returnObj;
    }
}
