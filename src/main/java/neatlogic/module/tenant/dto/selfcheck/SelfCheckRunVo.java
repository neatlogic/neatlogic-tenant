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

import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.util.SnowflakeUtil;

import java.util.Date;
import java.util.List;

public class SelfCheckRunVo extends BasePageVo {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long scenarioId;
    private String scenarioName;
    private Integer scenarioVersion;
    private String triggerType;
    private String status;
    private String runningKey;
    private String executorUuid;
    private String userName;
    private Integer serverId;
    private Integer itemCount;
    private Date startTime;
    private Date endTime;
    private Long durationMs;
    private Integer successCount;
    private Integer failedCount;
    private String errorMessage;
    private String fcu;
    private Date fcd;
    private List<SelfCheckRunItemVo> itemList;

    public Long getId() {
        if (id == null) {
            id = SnowflakeUtil.uniqueLong();
        }
        return id;
    }
    public void setId(Long id) { this.id = id; }
    public Long getScenarioId() { return scenarioId; }
    public void setScenarioId(Long scenarioId) { this.scenarioId = scenarioId; }
    public String getScenarioName() { return scenarioName; }
    public void setScenarioName(String scenarioName) { this.scenarioName = scenarioName; }
    public Integer getScenarioVersion() { return scenarioVersion; }
    public void setScenarioVersion(Integer scenarioVersion) { this.scenarioVersion = scenarioVersion; }
    public String getTriggerType() { return triggerType; }
    public void setTriggerType(String triggerType) { this.triggerType = triggerType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRunningKey() { return runningKey; }
    public void setRunningKey(String runningKey) { this.runningKey = runningKey; }
    public String getExecutorUuid() { return executorUuid; }
    public void setExecutorUuid(String executorUuid) { this.executorUuid = executorUuid; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Integer getServerId() { return serverId; }
    public void setServerId(Integer serverId) { this.serverId = serverId; }
    public Integer getItemCount() { return itemCount; }
    public void setItemCount(Integer itemCount) { this.itemCount = itemCount; }
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public Integer getSuccessCount() { return successCount; }
    public void setSuccessCount(Integer successCount) { this.successCount = successCount; }
    public Integer getFailedCount() { return failedCount; }
    public void setFailedCount(Integer failedCount) { this.failedCount = failedCount; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getFcu() { return fcu; }
    public void setFcu(String fcu) { this.fcu = fcu; }
    public Date getFcd() { return fcd; }
    public void setFcd(Date fcd) { this.fcd = fcd; }
    public List<SelfCheckRunItemVo> getItemList() { return itemList; }
    public void setItemList(List<SelfCheckRunItemVo> itemList) { this.itemList = itemList; }
}
