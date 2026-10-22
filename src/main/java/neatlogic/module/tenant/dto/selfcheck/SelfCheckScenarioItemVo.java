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
import com.alibaba.fastjson.annotation.JSONField;
import neatlogic.framework.util.SnowflakeUtil;

import java.io.Serializable;

public class SelfCheckScenarioItemVo implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long scenarioId;
    private String name;
    private Integer isActive;
    private Integer sort;
    private String apiToken;
    private String apiName;
    private String apiRisk;
    private String requestParam;
    private String verifyScript;
    private Integer isScriptValid;
    private String scriptDiagnosticList;
    private Integer apiTimeout;
    private Integer scriptTimeout;

    public Long getId() {
        if (id == null) {
            id = SnowflakeUtil.uniqueLong();
        }
        return id;
    }
    public void setId(Long id) { this.id = id; }
    public Long getScenarioId() { return scenarioId; }
    public void setScenarioId(Long scenarioId) { this.scenarioId = scenarioId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getIsActive() { return isActive; }
    public void setIsActive(Integer isActive) { this.isActive = isActive; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public String getApiToken() { return apiToken; }
    public void setApiToken(String apiToken) { this.apiToken = apiToken; }
    public String getApiName() { return apiName; }
    public void setApiName(String apiName) { this.apiName = apiName; }
    public String getApiRisk() { return apiRisk; }
    public void setApiRisk(String apiRisk) { this.apiRisk = apiRisk; }
    @JSONField(serialize = false)
    public String getRequestParam() { return requestParam; }
    public void setRequestParam(String requestParam) { this.requestParam = requestParam; }
    @JSONField(name = "requestParam")
    /** 将持久化请求参数恢复为 JSON 对象。 */
    public JSONObject getRequestParamObj() {
        if (requestParam == null) {
            return new JSONObject();
        }
        return JSON.parseObject(requestParam);
    }
    /** 将 JSON 请求参数序列化后持久化。 */
    public void setRequestParamObj(JSONObject requestParamObj) {
        if (requestParamObj == null) {
            this.requestParam = null;
        } else {
            this.requestParam = requestParamObj.toJSONString();
        }
    }
    public String getVerifyScript() { return verifyScript; }
    public void setVerifyScript(String verifyScript) { this.verifyScript = verifyScript; }
    public Integer getIsScriptValid() { return isScriptValid; }
    public void setIsScriptValid(Integer isScriptValid) { this.isScriptValid = isScriptValid; }
    @JSONField(serialize = false)
    public String getScriptDiagnosticList() { return scriptDiagnosticList; }
    public void setScriptDiagnosticList(String scriptDiagnosticList) { this.scriptDiagnosticList = scriptDiagnosticList; }
    @JSONField(name = "diagnosticList")
    /** 将脚本诊断快照恢复为 JSON 数组。 */
    public JSONArray getDiagnosticList() {
        if (scriptDiagnosticList == null) {
            return new JSONArray();
        }
        return JSON.parseArray(scriptDiagnosticList);
    }
    public Integer getApiTimeout() { return apiTimeout; }
    public void setApiTimeout(Integer apiTimeout) { this.apiTimeout = apiTimeout; }
    public Integer getScriptTimeout() { return scriptTimeout; }
    public void setScriptTimeout(Integer scriptTimeout) { this.scriptTimeout = scriptTimeout; }
}
