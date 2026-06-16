/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.tenant.api.loginaudit;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.common.constvalue.MimeType;
import neatlogic.framework.dao.mapper.LoginMapper;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.dto.loginaudit.LoginAuditSearchVo;
import neatlogic.framework.dto.loginaudit.LoginAuditVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.binarystream.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.util.FileUtil;
import neatlogic.framework.util.TimeUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ExportLoginAuditApi extends PrivateBinaryStreamApiComponentBase {

    private static final int EXPORT_PAGE_SIZE = 1000;
    private static final String[] HEADER_ARRAY = {"用户", "用户组", "IP", "登录时间", "登录方式"};

    @Resource
    private LoginMapper loginMapper;
    @Resource
    private TeamMapper teamMapper;
    @Resource
    private UserMapper userMapper;

    @Override
    public String getToken() {
        return "login/audit/export";
    }

    @Override
    public String getName() {
        return "导出登录记录列表";
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword"),
            @Param(name = "timeRange", type = ApiParamType.INTEGER, desc = "common.duration"),
            @Param(name = "timeUnit", type = ApiParamType.STRING, desc = "common.timeunit"),
            @Param(name = "startTime", type = ApiParamType.LONG, desc = "common.starttime"),
            @Param(name = "endTime", type = ApiParamType.LONG, desc = "common.endtime"),
            @Param(name = "teamUuidList", type = ApiParamType.JSONARRAY, desc = "common.teamuuidlist"),
    })
    @Description(desc = "导出登录记录列表")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        LoginAuditSearchVo searchVo = paramObj.toJavaObject(LoginAuditSearchVo.class);
        buildSearchParam(searchVo, paramObj);

        response.setContentType(MimeType.XLSX.getValue() + ";charset=utf-8");
        response.setHeader("Content-Disposition", " attachment; filename=\"" + FileUtil.getEncodedFileName("登录记录.xlsx") + "\"");
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(EXPORT_PAGE_SIZE);
             ServletOutputStream os = response.getOutputStream()) {
            Sheet sheet = workbook.createSheet("登录记录");
            writeHeader(workbook, sheet);
            writeData(sheet, searchVo);
            workbook.write(os);
            workbook.dispose();
        }
        return null;
    }

    private void buildSearchParam(LoginAuditSearchVo searchVo, JSONObject paramObj) {
        if (CollectionUtils.isNotEmpty(searchVo.getTeamUuidList())) {
            searchVo.setTeamUuidList(searchVo.getTeamUuidList().stream().map(GroupSearch::removePrefix).collect(Collectors.toList()));
        }
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

    private void writeHeader(SXSSFWorkbook workbook, Sheet sheet) {
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = workbook.createFont();
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < HEADER_ARRAY.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(HEADER_ARRAY[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 24 * 256);
        }
    }

    private void writeData(Sheet sheet, LoginAuditSearchVo searchVo) {
        int rowNum = loginMapper.getLoginAuditCount(searchVo);
        if (rowNum <= 0) {
            return;
        }
        searchVo.setRowNum(rowNum);
        int rowIndex = 1;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (int currentPage = 1; currentPage <= searchVo.getPageCount(); currentPage++) {
            searchVo.setCurrentPage(currentPage);
            List<LoginAuditVo> loginAuditList = loginMapper.getLoginAuditList(searchVo);
            if (CollectionUtils.isEmpty(loginAuditList)) {
                continue;
            }
            for (LoginAuditVo loginAuditVo : loginAuditList) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(getUserText(loginAuditVo.getUserUuid()));
                row.createCell(1).setCellValue(getTeamText(loginAuditVo.getUserUuid()));
                row.createCell(2).setCellValue(StringUtils.defaultString(loginAuditVo.getIp()));
                row.createCell(3).setCellValue(loginAuditVo.getLoginTime() == null ? StringUtils.EMPTY : dateFormat.format(loginAuditVo.getLoginTime()));
                row.createCell(4).setCellValue(StringUtils.defaultString(loginAuditVo.getLoginMethod()));
            }
        }
    }

    private String getUserText(String userUuid) {
        if (StringUtils.isBlank(userUuid)) {
            return StringUtils.EMPTY;
        }
        UserVo userVo = userMapper.getUserBaseInfoByUuid(userUuid);
        if (userVo == null) {
            return userUuid;
        }
        if (StringUtils.isNotBlank(userVo.getUserName()) && StringUtils.isNotBlank(userVo.getUserId())) {
            return userVo.getUserName() + "(" + userVo.getUserId() + ")";
        }
        return StringUtils.defaultIfBlank(userVo.getUserName(), StringUtils.defaultIfBlank(userVo.getUserId(), userUuid));
    }

    private String getTeamText(String userUuid) {
        if (StringUtils.isBlank(userUuid)) {
            return StringUtils.EMPTY;
        }
        List<TeamVo> teamList = teamMapper.getTeamListByUserUuid(userUuid);
        if (CollectionUtils.isEmpty(teamList)) {
            return StringUtils.EMPTY;
        }
        return teamList.stream().map(TeamVo::getName).collect(Collectors.joining(","));
    }

    @Override
    public String getConfig() {
        return null;
    }
}
