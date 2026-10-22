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
package neatlogic.module.tenant.dto.selfcheck;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.util.SnowflakeUtil;

import java.io.Serializable;
import java.util.Date;

public class SelfCheckRunItemVo implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long runId;
    private Long scenarioItemId;
    private String itemName;
    private Integer sort;
    private String apiToken;
    private String apiName;
    private String apiRisk;
    private String requestSnapshot;
    private String verifyScript;
    private Integer apiTimeout;
    private Integer scriptTimeout;
    private String status;
    private String phaseList;
    private String responseSnapshot;
    private String scriptResult;
    private String consoleLogList;
    private String diagnosticList;
    private String cleanupStatus;
    private Date startTime;
    private Date endTime;
    private Long durationMs;
    private String truncatedFieldList;
    private String traceId;

    public Long getId() {
        if (id == null) {
            id = SnowflakeUtil.uniqueLong();
        }
        return id;
    }
    public void setId(Long id) { this.id = id; }
    public Long getRunId() { return runId; }
    public void setRunId(Long runId) { this.runId = runId; }
    public Long getScenarioItemId() { return scenarioItemId; }
    public void setScenarioItemId(Long scenarioItemId) { this.scenarioItemId = scenarioItemId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public String getApiToken() { return apiToken; }
    public void setApiToken(String apiToken) { this.apiToken = apiToken; }
    public String getApiName() { return apiName; }
    public void setApiName(String apiName) { this.apiName = apiName; }
    public String getApiRisk() { return apiRisk; }
    public void setApiRisk(String apiRisk) { this.apiRisk = apiRisk; }
    public String getRequestSnapshot() { return requestSnapshot; }
    public void setRequestSnapshot(String requestSnapshot) { this.requestSnapshot = requestSnapshot; }
    public String getVerifyScript() { return verifyScript; }
    public void setVerifyScript(String verifyScript) { this.verifyScript = verifyScript; }
    public Integer getApiTimeout() { return apiTimeout; }
    public void setApiTimeout(Integer apiTimeout) { this.apiTimeout = apiTimeout; }
    public Integer getScriptTimeout() { return scriptTimeout; }
    public void setScriptTimeout(Integer scriptTimeout) { this.scriptTimeout = scriptTimeout; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPhaseList() { return phaseList; }
    public void setPhaseList(String phaseList) { this.phaseList = phaseList; }
    public String getResponseSnapshot() { return responseSnapshot; }
    public void setResponseSnapshot(String responseSnapshot) { this.responseSnapshot = responseSnapshot; }
    public String getScriptResult() { return scriptResult; }
    public void setScriptResult(String scriptResult) { this.scriptResult = scriptResult; }
    public String getConsoleLogList() { return consoleLogList; }
    public void setConsoleLogList(String consoleLogList) { this.consoleLogList = consoleLogList; }
    public String getDiagnosticList() { return diagnosticList; }
    public void setDiagnosticList(String diagnosticList) { this.diagnosticList = diagnosticList; }
    public String getCleanupStatus() { return cleanupStatus; }
    public void setCleanupStatus(String cleanupStatus) { this.cleanupStatus = cleanupStatus; }
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public String getTruncatedFieldList() { return truncatedFieldList; }
    public void setTruncatedFieldList(String truncatedFieldList) { this.truncatedFieldList = truncatedFieldList; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    /** 将持久化 JSON 字符串转换为执行详情页面使用的结构。 */
    public JSONObject toDetailJson() {
        JSONObject result = (JSONObject) JSON.toJSON(this);
        result.put("phaseList", parseArray(phaseList));
        result.put("requestSnapshot", parseValue(requestSnapshot));
        result.put("responseSnapshot", parseValue(responseSnapshot));
        result.put("scriptResult", parseValue(scriptResult));
        result.put("consoleLogList", parseArray(consoleLogList));
        result.put("diagnosticList", parseArray(diagnosticList));
        JSONArray truncatedList = parseArray(truncatedFieldList);
        result.put("truncatedFieldList", truncatedList);
        result.put("requestTruncated", truncatedList.contains("requestSnapshot"));
        result.put("responseTruncated", truncatedList.contains("responseSnapshot"));
        result.put("scriptResultTruncated", truncatedList.contains("scriptResult"));
        result.put("consoleTruncated", truncatedList.contains("consoleLogList"));
        return result;
    }

    /** 将可解析的 JSON 文本恢复为对象，普通文本保持原值。 */
    private Object parseValue(String value) {
        if (value == null) {
            return null;
        }
        try {
            return JSON.parse(value);
        } catch (Exception ignored) {
            return value;
        }
    }

    /** 将数组快照恢复为 JSON 数组，空值返回空数组。 */
    private JSONArray parseArray(String value) {
        if (value == null) {
            return new JSONArray();
        }
        try {
            JSONArray result = JSON.parseArray(value);
            if (result == null) {
                return new JSONArray();
            }
            return result;
        } catch (Exception ignored) {
            return new JSONArray();
        }
    }
}
