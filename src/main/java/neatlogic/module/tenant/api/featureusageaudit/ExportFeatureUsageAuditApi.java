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
import neatlogic.framework.common.constvalue.MimeType;
import neatlogic.framework.common.util.ModuleUtil;
import neatlogic.framework.dao.mapper.FeatureUsageAuditMapper;
import neatlogic.framework.dto.featureusageaudit.FeatureUsageAuditSearchVo;
import neatlogic.framework.dto.featureusageaudit.FeatureUsageAuditVo;
import neatlogic.framework.dto.module.ModuleGroupVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.binarystream.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.util.FileUtil;
import neatlogic.framework.util.TimeUtil;
import neatlogic.framework.util.excel.ExcelBuilder;
import neatlogic.framework.util.excel.SheetBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ExportFeatureUsageAuditApi extends PrivateBinaryStreamApiComponentBase {

    private static final int EXPORT_PAGE_SIZE = 100;
    private static final List<String> HEADER_LIST = Arrays.asList("模块", "功能名称", "使用次数");
    private static final List<String> COLUMN_LIST = Arrays.asList("moduleGroupName", "featureName", "usedCount");

    @Resource
    private FeatureUsageAuditMapper featureUsageAuditMapper;

    @Override
    public String getToken() {
        return "feature/usage/audit/export";
    }

    @Override
    public String getName() {
        return "导出功能使用统计数据";
    }

    @Input({
            @Param(name = "userUuid", type = ApiParamType.STRING, desc = "用户UUID"),
            @Param(name = "moduleGroupList", type = ApiParamType.JSONARRAY, desc = "模块列表"),
            @Param(name = "featureNameList", type = ApiParamType.JSONARRAY, desc = "功能列表"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword"),
            @Param(name = "timeRange", type = ApiParamType.INTEGER, desc = "common.duration"),
            @Param(name = "timeUnit", type = ApiParamType.STRING, desc = "common.timeunit"),
            @Param(name = "startTime", type = ApiParamType.LONG, desc = "common.starttime"),
            @Param(name = "endTime", type = ApiParamType.LONG, desc = "common.endtime"),
    })
    @Output({})
    @Description(desc = "导出功能使用统计数据")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        FeatureUsageAuditSearchVo searchVo = paramObj.toJavaObject(FeatureUsageAuditSearchVo.class);
        buildSearchParam(searchVo, paramObj);

        response.setContentType(MimeType.XLSX.getValue() + ";charset=utf-8");
        String fileName = "功能使用统计" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date()) + ".xlsx";
        response.setHeader("Content-Disposition", " attachment; filename=\"" + FileUtil.getEncodedFileName(fileName) + "\"");
        ExcelBuilder builder = new ExcelBuilder(SXSSFWorkbook.class)
                .withBorderColor(HSSFColor.HSSFColorPredefined.GREY_40_PERCENT)
                .withHeadFontColor(HSSFColor.HSSFColorPredefined.WHITE)
                .withHeadBgColor(HSSFColor.HSSFColorPredefined.DARK_BLUE)
                .withColumnWidth(24);
        SheetBuilder sheetBuilder = builder.addSheet("sheet1")
                .withHeaderList(HEADER_LIST)
                .withColumnList(COLUMN_LIST);
        try (Workbook workbook = builder.build();
             ServletOutputStream os = response.getOutputStream()) {
            writeData(sheetBuilder, searchVo);
            workbook.write(os);
            if (workbook instanceof SXSSFWorkbook) {
                ((SXSSFWorkbook) workbook).dispose();
            }
        }
        return null;
    }

    private void buildSearchParam(FeatureUsageAuditSearchVo searchVo, JSONObject paramObj) {
        String userUuid = paramObj.getString("userUuid");
        if (StringUtils.isNotBlank(userUuid) && userUuid.startsWith(GroupSearch.USER.getValuePlugin())) {
            searchVo.setUserUuid(GroupSearch.removePrefix(userUuid));
        }
        // 将相对时间范围转换为开始时间和结束时间，导出与列表查询使用同一套时间条件。
        if (searchVo.getStartTime() == null && searchVo.getEndTime() == null) {
            Integer timeRange = paramObj.getInteger("timeRange");
            String timeUnit = paramObj.getString("timeUnit");
            if (timeRange != null && StringUtils.isNotBlank(timeUnit)) {
                searchVo.setStartTime(TimeUtil.recentTimeTransfer(timeRange, timeUnit));
                searchVo.setEndTime(new Date());
            }
        }
        searchVo.setCurrentPage(1);
        searchVo.setPageSize(EXPORT_PAGE_SIZE);
    }

    private void writeData(SheetBuilder sheetBuilder, FeatureUsageAuditSearchVo searchVo) {
        int rowNum = featureUsageAuditMapper.getFeatureCount(searchVo);
        if (rowNum <= 0) {
            return;
        }
        searchVo.setRowNum(rowNum);
        Integer pageCount = searchVo.getPageCount();
        for (int currentPage = 1; currentPage <= pageCount; currentPage++) {
            searchVo.setCurrentPage(currentPage);
            List<FeatureUsageAuditVo> featureUsageAuditList = featureUsageAuditMapper.getFeatureList(searchVo);
            for (FeatureUsageAuditVo featureUsageAuditVo : featureUsageAuditList) {
                Map<String, Object> dataMap = new LinkedHashMap<>();
                dataMap.put("moduleGroupName", getModuleGroupName(featureUsageAuditVo.getModuleGroup()));
                dataMap.put("featureName", StringUtils.defaultString(featureUsageAuditVo.getFeatureName()));
                dataMap.put("usedCount", featureUsageAuditVo.getUsedCount() == null ? 0 : featureUsageAuditVo.getUsedCount());
                sheetBuilder.addData(dataMap);
            }
        }
    }

    private String getModuleGroupName(String moduleGroup) {
        ModuleGroupVo groupVo = ModuleUtil.getModuleGroup(moduleGroup);
        if (groupVo != null) {
            return groupVo.getGroupName();
        }
        return StringUtils.defaultString(moduleGroup);
    }

    @Override
    public String getConfig() {
        return null;
    }
}
