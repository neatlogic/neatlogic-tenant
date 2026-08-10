/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.tenant.utils;

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.common.util.ModuleUtil;
import neatlogic.framework.dto.module.ModuleGroupVo;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.portal.widget.core.IPortalWidget;
import neatlogic.framework.portal.widget.core.PortalWidgetFactory;
import neatlogic.framework.util.$;
import neatlogic.module.tenant.dto.portal.PortalWidgetSearchVo;
import neatlogic.module.tenant.dto.portal.PortalWidgetVo;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class PortalWidgetSearchUtil {

    public static List<PortalWidgetVo> getPortalWidgetList(PortalWidgetSearchVo searchVo) {
        List<PortalWidgetVo> allPortalWidgetList = new ArrayList<>();
        if (Objects.equals(searchVo.getModuleGroup(), "index")) {
            List<ModuleGroupVo> activeModuleGroupList = TenantContext.get().getActiveModuleGroupList();
            for (ModuleGroupVo moduleGroupVo : activeModuleGroupList) {
                List<IPortalWidget> widgetList = PortalWidgetFactory.getPortalWidgetListByModuleGroup(moduleGroupVo.getGroup());
                for (IPortalWidget widget : widgetList) {
                    PortalWidgetVo portalWidgetVo = new PortalWidgetVo();
                    portalWidgetVo.setName(widget.getValue());
                    portalWidgetVo.setLabel(widget.getText());
                    portalWidgetVo.setSort(widget.getSort());
                    ModuleGroupVo newModuleGroupVo = new ModuleGroupVo();
                    if (Objects.equals(moduleGroupVo.getGroup(), "framework")) {
                        newModuleGroupVo.setGroup(moduleGroupVo.getGroup());
                        newModuleGroupVo.setGroupName("common.common");
                        newModuleGroupVo.setGroupSort(0);
                        newModuleGroupVo.setGroupDescription(moduleGroupVo.getGroupDescription());
                    } else {
                        newModuleGroupVo.setGroup(moduleGroupVo.getGroup());
                        newModuleGroupVo.setGroupName(moduleGroupVo.getGroupName());
                        newModuleGroupVo.setGroupSort(moduleGroupVo.getGroupSort());
                        newModuleGroupVo.setGroupDescription(moduleGroupVo.getGroupDescription());
                    }
                    portalWidgetVo.setModuleGroup(newModuleGroupVo);
                    allPortalWidgetList.add(portalWidgetVo);
                }
            }
        } else {
            ModuleGroupVo moduleGroupVo = ModuleUtil.getModuleGroup(searchVo.getModuleGroup());
            if (moduleGroupVo == null) {
                throw new ParamIrregularException("moduleGroup", $.t("nfem.modulegroupnotfoundexception.modulegroupnotfoundexception", searchVo.getModuleGroup()));
            }
            {
                ModuleGroupVo frameworkModuleGroupVo = ModuleUtil.getModuleGroup("framework");
                List<IPortalWidget> widgetList = PortalWidgetFactory.getPortalWidgetListByModuleGroup(frameworkModuleGroupVo.getGroup());
                for (IPortalWidget widget : widgetList) {
                    PortalWidgetVo portalWidgetVo = new PortalWidgetVo();
                    portalWidgetVo.setName(widget.getValue());
                    portalWidgetVo.setLabel(widget.getText());
                    portalWidgetVo.setSort(widget.getSort());
                    ModuleGroupVo newModuleGroupVo = new ModuleGroupVo();
                    newModuleGroupVo.setGroup(frameworkModuleGroupVo.getGroup());
                    newModuleGroupVo.setGroupName("common.common");
                    newModuleGroupVo.setGroupSort(0);
                    newModuleGroupVo.setGroupDescription(frameworkModuleGroupVo.getGroupDescription());
                    portalWidgetVo.setModuleGroup(newModuleGroupVo);
                    allPortalWidgetList.add(portalWidgetVo);
                }
            }
            List<IPortalWidget> widgetList = PortalWidgetFactory.getPortalWidgetListByModuleGroup(searchVo.getModuleGroup());
            for (IPortalWidget widget : widgetList) {
                PortalWidgetVo portalWidgetVo = new PortalWidgetVo();
                portalWidgetVo.setName(widget.getValue());
                portalWidgetVo.setLabel(widget.getText());
                portalWidgetVo.setSort(widget.getSort());
                ModuleGroupVo newModuleGroupVo = new ModuleGroupVo();
                newModuleGroupVo.setGroup(moduleGroupVo.getGroup());
                newModuleGroupVo.setGroupName(moduleGroupVo.getGroupName());
                newModuleGroupVo.setGroupSort(moduleGroupVo.getGroupSort());
                newModuleGroupVo.setGroupDescription(moduleGroupVo.getGroupDescription());
                portalWidgetVo.setModuleGroup(newModuleGroupVo);
                allPortalWidgetList.add(portalWidgetVo);
            }
        }

        List<PortalWidgetVo> portalWidgetList = new ArrayList<>();
        for (PortalWidgetVo portalWidgetVo : allPortalWidgetList) {
            if (StringUtils.isBlank(searchVo.getKeyword())
                    || StringUtils.containsIgnoreCase(portalWidgetVo.getName(), searchVo.getKeyword())
                    || StringUtils.containsIgnoreCase(portalWidgetVo.getLabel(), searchVo.getKeyword())) {
                portalWidgetList.add(portalWidgetVo);
            }
        }
        portalWidgetList.sort(new Comparator<PortalWidgetVo>() {
            @Override
            public int compare(PortalWidgetVo o1, PortalWidgetVo o2) {
                ModuleGroupVo moduleGroup1 = o1.getModuleGroup();
                ModuleGroupVo moduleGroup2 = o2.getModuleGroup();
                int j = moduleGroup1.getGroupSort().compareTo(moduleGroup2.getGroupSort());
                if (j == 0) {
                    return o1.getSort().compareTo(o2.getSort());
                } else {
                    return j;
                }
            }
        });
        return portalWidgetList;
    }
}
