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

import neatlogic.framework.common.dto.BaseEditorVo;

import java.util.List;

public class PortalSearchVo extends BaseEditorVo {
    private static final long serialVersionUID = 1L;

    private Integer isActive;
    private String moduleGroup;
    private String type;
    private List<Long> globalPortalIdList;

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

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

    public List<Long> getGlobalPortalIdList() {
        return globalPortalIdList;
    }

    public void setGlobalPortalIdList(List<Long> globalPortalIdList) {
        this.globalPortalIdList = globalPortalIdList;
    }
}
