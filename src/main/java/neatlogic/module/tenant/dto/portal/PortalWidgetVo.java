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

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.WorkAssignmentUnitVo;
import neatlogic.framework.dto.module.ModuleGroupVo;
import neatlogic.framework.restful.annotation.EntityField;

import java.util.List;

public class PortalWidgetVo {
    @EntityField(name = "common.name", type = ApiParamType.STRING)
    private String name;
    @EntityField(name = "common.label", type = ApiParamType.STRING)
    private String label;
    @EntityField(name = "common.sort", type = ApiParamType.INTEGER)
    private Integer sort;
    @EntityField(name = "common.modulegroup", type = ApiParamType.JSONOBJECT)
    private ModuleGroupVo moduleGroup;
    @EntityField(name = "common.authoritylist", type = ApiParamType.JSONARRAY)
    private List<String> authorityList;
    @EntityField(name = "common.authoritylist", type = ApiParamType.JSONARRAY)
    private List<WorkAssignmentUnitVo> authorityVoList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public ModuleGroupVo getModuleGroup() {
        return moduleGroup;
    }

    public void setModuleGroup(ModuleGroupVo moduleGroup) {
        this.moduleGroup = moduleGroup;
    }

    public List<String> getAuthorityList() {
        return authorityList;
    }

    public void setAuthorityList(List<String> authorityList) {
        this.authorityList = authorityList;
    }

    public List<WorkAssignmentUnitVo> getAuthorityVoList() {
        return authorityVoList;
    }

    public void setAuthorityVoList(List<WorkAssignmentUnitVo> authorityVoList) {
        this.authorityVoList = authorityVoList;
    }
}
