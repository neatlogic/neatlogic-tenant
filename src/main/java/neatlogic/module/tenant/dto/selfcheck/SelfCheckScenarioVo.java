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

public class SelfCheckScenarioVo extends BasePageVo {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String name;
    private String description;
    private String status;
    private Integer version;
    private String fcu;
    private Date fcd;
    private String lcu;
    private Date lcd;
    private Integer itemCount;
    private Integer isScriptValid;
    private List<SelfCheckScenarioItemVo> itemList;

    public Long getId() {
        if (id == null) {
            id = SnowflakeUtil.uniqueLong();
        }
        return id;
    }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public String getFcu() { return fcu; }
    public void setFcu(String fcu) { this.fcu = fcu; }
    public Date getFcd() { return fcd; }
    public void setFcd(Date fcd) { this.fcd = fcd; }
    public String getLcu() { return lcu; }
    public void setLcu(String lcu) { this.lcu = lcu; }
    public Date getLcd() { return lcd; }
    public void setLcd(Date lcd) { this.lcd = lcd; }
    public Integer getItemCount() { return itemCount; }
    public void setItemCount(Integer itemCount) { this.itemCount = itemCount; }
    public Integer getIsScriptValid() { return isScriptValid; }
    public void setIsScriptValid(Integer isScriptValid) { this.isScriptValid = isScriptValid; }
    public List<SelfCheckScenarioItemVo> getItemList() { return itemList; }
    public void setItemList(List<SelfCheckScenarioItemVo> itemList) { this.itemList = itemList; }
}
