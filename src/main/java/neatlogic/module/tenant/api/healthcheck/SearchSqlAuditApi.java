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
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dao.plugin.SqlCostInterceptor;
import neatlogic.framework.dto.healthcheck.RequestSqlAuditVo;
import neatlogic.framework.dto.healthcheck.SqlAuditVo;
import neatlogic.framework.healthcheck.SqlAuditManager;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Comparator;
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
        return "nmtah.searchsqlauditapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    /** 使用同一次缓存快照查询两类监控记录，支持表头升降序及保存模式默认排序。 */
    @Input({@Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword"),
            @Param(name = "tenant", type = ApiParamType.STRING, desc = "common.tenant"),
            @Param(name = "userId", type = ApiParamType.STRING, desc = "common.userid"),
            @Param(name = "orderBy", type = ApiParamType.ENUM, rule = "timecost,runtime", desc = "排序，只支持timecost和runtime"),
            @Param(name = "orderType", type = ApiParamType.ENUM, rule = "ASC,DESC", defaultValue = "DESC", desc = "排序方向，默认DESC"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
            @Param(name = "requestCurrentPage", type = ApiParamType.INTEGER, desc = "common.currentpage")
    })
    @Output({
            @Param(name = "sqlAuditData", type = ApiParamType.JSONOBJECT, desc = "common.tbodylist"),
            @Param(name = "requestSqlAuditData", type = ApiParamType.JSONOBJECT, desc = "common.tbodylist"),
            @Param(name = "saveMode", type = ApiParamType.ENUM, rule = "recent,slowest", desc = "nmtah.updatesqlauditmodeapi.savemode")
    })
    @Description(desc = "nmtah.searchsqlauditapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        SqlAuditManager.Snapshot snapshot = SqlAuditManager.getSnapshot();
        String orderBy = paramObj.getString("orderBy");
        if (StringUtils.isBlank(orderBy)) {
            if (Objects.equals(snapshot.getSaveMode(), "slowest")) {
                orderBy = "timecost";
            } else {
                orderBy = "runtime";
            }
        }
        orderBy = orderBy.toLowerCase(Locale.ROOT);
        // 框架枚举校验不区分大小写，缺省仍按降序兼容已有调用方。
        boolean ascending = StringUtils.equalsIgnoreCase(paramObj.getString("orderType"), "ASC");
        String keyword = paramObj.getString("keyword");
        String tenant = paramObj.getString("tenant");
        String userId = paramObj.getString("userId");
        // SQL ID监控和URL监控共用keyword、tenant、userId过滤条件，但仍分别分页避免两个Tab互相影响页码
        JSONObject sqlAuditData = buildSqlAuditData(snapshot.getSqlAuditList(), keyword, tenant, userId, orderBy, ascending, paramObj.getInteger("currentPage"));
        JSONObject requestSqlAuditData = buildRequestSqlAuditData(snapshot.getRequestSqlAuditList(), keyword, tenant, userId, orderBy, ascending, paramObj.getInteger("requestCurrentPage"));
        JSONObject returnObj = new JSONObject();
        returnObj.put("saveMode", snapshot.getSaveMode());
        returnObj.put("sqlAuditData", sqlAuditData);
        returnObj.put("requestSqlAuditData", requestSqlAuditData);
        returnObj.put("maxTimeCost", sqlAuditData.getLongValue("maxTimeCost"));
        returnObj.put("maxRequestTimeCost", requestSqlAuditData.getLongValue("maxRequestTimeCost"));
        returnObj.put("sqlIdList", SqlCostInterceptor.SqlIdMap.getSqlIdList());
        returnObj.put("urlList", SqlCostInterceptor.UrlMap.getUrlList());
        return returnObj;
    }

    /** 在 SQL 快照内应用过滤、稳定排序及独立分页，不修改缓存。 */
    private JSONObject buildSqlAuditData(List<SqlAuditVo> sqlAuditList, String keyword, String tenant, String userId, String orderBy, boolean ascending, Integer currentPage) {
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
        Comparator<SqlAuditVo> comparator;
        if (Objects.equals(orderBy, "timecost")) {
            comparator = Comparator.comparing(SqlAuditVo::getTimeCost);
        } else {
            comparator = Comparator.comparing(SqlAuditVo::getRunTime);
        }
        if (!ascending) {
            comparator = comparator.reversed();
        }
        // 仅反转比较器，避免倒转整张列表导致同值记录丢失快照顺序。
        sqlAuditList = sqlAuditList.stream().sorted(comparator).collect(Collectors.toList());
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

    /** 在请求快照内按累计 SQL 耗时或发生时间升降序排序，保留请求聚合信息。 */
    private JSONObject buildRequestSqlAuditData(List<RequestSqlAuditVo> requestSqlAuditList, String keyword, String tenant, String userId, String orderBy, boolean ascending, Integer currentPage) {
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
        Comparator<RequestSqlAuditVo> comparator;
        if (Objects.equals(orderBy, "timecost")) {
            comparator = Comparator.comparingLong(RequestSqlAuditVo::getTotalTimeCost);
        } else {
            comparator = Comparator.comparing(RequestSqlAuditVo::getRunTime);
        }
        if (!ascending) {
            comparator = comparator.reversed();
        }
        requestSqlAuditList = requestSqlAuditList.stream().sorted(comparator).collect(Collectors.toList());
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

    /** 对 SQL ID 和 URL 关键字进行忽略大小写的包含匹配。 */
    private boolean containsIgnoreCase(String source, String keyword) {
        // SQL监控关键字统一使用忽略大小写的包含匹配，兼容原SQL ID和URL搜索体验
        return StringUtils.isNotBlank(source) && StringUtils.isNotBlank(keyword) && source.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    /** 对独立快照进行分页，并将未传或越界的页码限制在有效范围内。 */
    private JSONObject pageList(List<?> list, Integer currentPage) {
        int rowNum = list.size();
        int pageCount = PageUtil.getPageCount(rowNum, PAGE_SIZE);
        // 空列表也返回第1页，避免前端表格收到currentPage=0
        pageCount = Math.max(1, pageCount);
        if (currentPage == null) {
            currentPage = 1;
        }
        currentPage = Math.max(1, currentPage);
        currentPage = Math.min(currentPage, pageCount);
        List<?> tbodyList = list.stream().skip((long) (currentPage - 1) * PAGE_SIZE).limit(PAGE_SIZE).collect(Collectors.toList());
        JSONObject returnObj = new JSONObject();
        returnObj.put("pageCount", pageCount);
        returnObj.put("currentPage", currentPage);
        returnObj.put("pageSize", PAGE_SIZE);
        returnObj.put("rowNum", rowNum);
        returnObj.put("tbodyList", tbodyList);
        return returnObj;
    }
}
