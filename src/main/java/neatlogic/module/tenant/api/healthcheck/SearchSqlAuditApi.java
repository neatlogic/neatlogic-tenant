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

    @Input({@Param(name = "id", type = ApiParamType.STRING, desc = "sql语句id"),
            @Param(name = "url", type = ApiParamType.STRING, desc = "URL监控按请求地址过滤"),
            @Param(name = "orderBy", type = ApiParamType.ENUM, rule = "timecost,runtime", desc = "排序，只支持timecost和runtime"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "SQL ID监控当前页"),
            @Param(name = "requestCurrentPage", type = ApiParamType.INTEGER, desc = "URL监控当前页")
    })
    @Output({
            @Param(name = "sqlAuditData", explode = BasePageVo.class),
            @Param(name = "requestSqlAuditData", explode = BasePageVo.class)
    })
    @Description(desc = "获取SQL耗时接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String orderBy = StringUtils.isNotBlank(paramObj.getString("orderBy")) ? paramObj.getString("orderBy") : "runtime";
        // SQL ID监控和URL监控分别分页，避免两个Tab切换时互相影响页码
        JSONObject sqlAuditData = buildSqlAuditData(paramObj.getString("id"), orderBy, paramObj.getIntValue("currentPage"));
        JSONObject requestSqlAuditData = buildRequestSqlAuditData(paramObj.getString("url"), orderBy, paramObj.getIntValue("requestCurrentPage"));
        JSONObject returnObj = new JSONObject();
        returnObj.put("sqlAuditData", sqlAuditData);
        returnObj.put("requestSqlAuditData", requestSqlAuditData);
        returnObj.put("maxTimeCost", sqlAuditData.getLongValue("maxTimeCost"));
        returnObj.put("maxRequestTimeCost", requestSqlAuditData.getLongValue("maxRequestTimeCost"));
        returnObj.put("sqlIdList", SqlCostInterceptor.SqlIdMap.getSqlIdList());
        returnObj.put("urlList", SqlCostInterceptor.UrlMap.getUrlList());
        return returnObj;
    }

    private JSONObject buildSqlAuditData(String id, String orderBy, int currentPage) {
        // 复制新列表，避免前端分页和排序影响内存中的原始审计列表
        List<SqlAuditVo> sqlAuditList = new ArrayList<>(SqlAuditManager.getSqlAuditList());
        if (StringUtils.isNotBlank(id)) {
            sqlAuditList = sqlAuditList.stream().filter(d -> StringUtils.isNotBlank(d.getId()) && d.getId().toLowerCase(Locale.ROOT).contains(id.toLowerCase(Locale.ROOT))).collect(Collectors.toList());
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

    private JSONObject buildRequestSqlAuditData(String url, String orderBy, int currentPage) {
        // URL监控列表按一次HTTP请求一行展示，和SQL ID明细列表完全分开
        List<RequestSqlAuditVo> requestSqlAuditList = new ArrayList<>(SqlAuditManager.getRequestSqlAuditList());
        if (StringUtils.isNotBlank(url)) {
            requestSqlAuditList = requestSqlAuditList.stream().filter(d -> StringUtils.isNotBlank(d.getUrl()) && d.getUrl().toLowerCase(Locale.ROOT).contains(url.toLowerCase(Locale.ROOT))).collect(Collectors.toList());
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
