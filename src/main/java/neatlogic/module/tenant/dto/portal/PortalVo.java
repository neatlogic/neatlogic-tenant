/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.tenant.dto.portal;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BaseEditorVo;
import neatlogic.framework.dto.WorkAssignmentUnitVo;
import neatlogic.framework.restful.annotation.EntityField;

import java.util.List;

public class PortalVo extends BaseEditorVo {
    private static final long serialVersionUID = 1L;

    @EntityField(name = "common.id", type = ApiParamType.LONG)
    private Long id;
    @EntityField(name = "common.name", type = ApiParamType.STRING)
    private String name;
    @EntityField(name = "common.isactive", type = ApiParamType.INTEGER)
    private Integer isActive;
    @EntityField(name = "common.modulegroup", type = ApiParamType.STRING)
    private String moduleGroup;
    @EntityField(name = "common.type", type = ApiParamType.STRING)
    private String type;
    @EntityField(name = "common.config", type = ApiParamType.JSONOBJECT)
    private JSONObject config;
    @JSONField(serialize = false)
    private String configStr;
    @EntityField(name = "common.sort", type = ApiParamType.INTEGER)
    private Integer sort;
    @EntityField(name = "common.authoritylist", type = ApiParamType.JSONARRAY)
    private List<String> authorityList;
    @EntityField(name = "common.authoritylist", type = ApiParamType.JSONARRAY)
    private List<WorkAssignmentUnitVo> authorityVoList;
    @EntityField(name = "nfpds.scoretemplatevo.entityfield.isactive.name", type = ApiParamType.INTEGER)
    private Integer isEnable;

    public Long getId() {
        return id;
    }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getIsActive() { return isActive; }
    public void setIsActive(Integer isActive) { this.isActive = isActive; }

    public String getModuleGroup() {
        return moduleGroup;
    }

    public void setModuleGroup(String moduleGroup) {
        this.moduleGroup = moduleGroup;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JSONObject getConfig() {
        if (config == null && configStr != null) { config = JSONObject.parseObject(configStr); }
        return config;
    }
    public void setConfig(JSONObject config) { this.config = config; }
    public String getConfigStr() {
        if (configStr == null && config != null) { configStr = config.toJSONString(); }
        return configStr;
    }
    public void setConfigStr(String configStr) { this.configStr = configStr; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public List<String> getAuthorityList() { return authorityList; }
    public void setAuthorityList(List<String> authorityList) { this.authorityList = authorityList; }

    public List<WorkAssignmentUnitVo> getAuthorityVoList() {
        return authorityVoList;
    }

    public void setAuthorityVoList(List<WorkAssignmentUnitVo> authorityVoList) {
        this.authorityVoList = authorityVoList;
    }

    public Integer getIsEnable() {
        return isEnable;
    }

    public void setIsEnable(Integer isEnable) {
        this.isEnable = isEnable;
    }
}
