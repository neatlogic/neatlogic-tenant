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
import neatlogic.framework.util.excel.ExcelBuilder;
import neatlogic.framework.util.excel.SheetBuilder;
import org.apache.commons.collections4.CollectionUtils;
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
import java.util.*;
import java.util.stream.Collectors;

@Component
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ExportLoginAuditApi extends PrivateBinaryStreamApiComponentBase {

    private static final int EXPORT_PAGE_SIZE = 100;
    private static final List<String> HEADER_LIST = Arrays.asList("用户", "用户组", "IP", "登录时间", "登录方式");
    private static final List<String> COLUMN_LIST = Arrays.asList("user", "teamNameList", "ip", "loginTime", "loginMethod");

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
        return "nmtal.exportloginauditapi.getname";
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword"),
            @Param(name = "timeRange", type = ApiParamType.INTEGER, desc = "common.duration"),
            @Param(name = "timeUnit", type = ApiParamType.STRING, desc = "common.timeunit"),
            @Param(name = "startTime", type = ApiParamType.LONG, desc = "common.starttime"),
            @Param(name = "endTime", type = ApiParamType.LONG, desc = "common.endtime"),
            @Param(name = "teamUuidList", type = ApiParamType.JSONARRAY, desc = "common.teamuuidlist"),
            @Param(name = "moduleGroupList", type = ApiParamType.JSONARRAY, desc = "common.modulegroup"),
            @Param(name = "featureNameList", type = ApiParamType.JSONARRAY, desc = "功能"),
    })
    @Description(desc = "nmtal.exportloginauditapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        LoginAuditSearchVo searchVo = paramObj.toJavaObject(LoginAuditSearchVo.class);
        buildSearchParam(searchVo, paramObj);

        response.setContentType(MimeType.XLSX.getValue() + ";charset=utf-8");
        String fileName = "登录记录" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date()) + ".xlsx";
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

    private void writeData(SheetBuilder sheetBuilder, LoginAuditSearchVo searchVo) {
        int rowNum = loginMapper.getLoginAuditCount(searchVo);
        if (rowNum <= 0) {
            return;
        }
        searchVo.setRowNum(rowNum);
        Integer pageCount = searchVo.getPageCount();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String, List<String>> userUuid2teamNameListMap = new HashMap<>();
        Map<String, UserVo> userMap = new HashMap<>();
        for (int currentPage = 1; currentPage <= pageCount; currentPage++) {
            searchVo.setCurrentPage(currentPage);
            List<LoginAuditVo> loginAuditList = loginMapper.getLoginAuditList(searchVo);
            if (CollectionUtils.isEmpty(loginAuditList)) {
                continue;
            }
            Set<String> userUuidSet = new HashSet<>();
            for (LoginAuditVo loginAuditVo : loginAuditList) {
                if (StringUtils.isNotBlank(loginAuditVo.getUserUuid())) {
                    if (!userMap.containsKey(loginAuditVo.getUserUuid())) {
                        userUuidSet.add(loginAuditVo.getUserUuid());
                    }
                    List<String> teamNameList = userUuid2teamNameListMap.get(loginAuditVo.getUserUuid());
                    if (teamNameList == null) {
                        teamNameList = new ArrayList<>();
                        List<TeamVo> teamList = teamMapper.getTeamListByUserUuid(loginAuditVo.getUserUuid());
                        if (CollectionUtils.isNotEmpty(teamList)) {
                            teamNameList = teamList.stream().map(TeamVo::getName).collect(Collectors.toList());
                        }
                        userUuid2teamNameListMap.put(loginAuditVo.getUserUuid(), teamNameList);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(userUuidSet)) {
                List<UserVo> userList = userMapper.getUserByUserUuidList(new ArrayList<>(userUuidSet));
                for (UserVo userVo : userList) {
                    userMap.put(userVo.getUuid(), userVo);
                }
            }
            for (LoginAuditVo loginAuditVo : loginAuditList) {
                Map<String, Object> dataMap = new LinkedHashMap<>();
                dataMap.put("user", getUserText(loginAuditVo.getUserUuid(), userMap.get(loginAuditVo.getUserUuid())));
                dataMap.put("teamNameList", getTeamText(userUuid2teamNameListMap.get(loginAuditVo.getUserUuid())));
                dataMap.put("ip", StringUtils.defaultString(loginAuditVo.getIp()));
                dataMap.put("loginTime", loginAuditVo.getLoginTime() == null ? StringUtils.EMPTY : dateFormat.format(loginAuditVo.getLoginTime()));
                dataMap.put("loginMethod", StringUtils.defaultString(loginAuditVo.getLoginMethod()));
                sheetBuilder.addData(dataMap);
            }
        }
    }

    private String getUserText(String userUuid, UserVo userVo) {
        if (StringUtils.isBlank(userUuid)) {
            return StringUtils.EMPTY;
        }
        if (userVo == null) {
            return userUuid;
        }
        if (StringUtils.isNotBlank(userVo.getUserName()) && StringUtils.isNotBlank(userVo.getUserId())) {
            return userVo.getUserName() + "(" + userVo.getUserId() + ")";
        }
        return StringUtils.defaultIfBlank(userVo.getUserName(), StringUtils.defaultIfBlank(userVo.getUserId(), userUuid));
    }

    private String getTeamText(List<String> teamNameList) {
        if (CollectionUtils.isEmpty(teamNameList)) {
            return StringUtils.EMPTY;
        }
        return String.join(",", teamNameList);
    }

    @Override
    public String getConfig() {
        return null;
    }
}
