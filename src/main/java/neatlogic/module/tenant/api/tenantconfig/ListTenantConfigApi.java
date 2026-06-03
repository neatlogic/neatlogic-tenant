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

package neatlogic.module.tenant.api.tenantconfig;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.ModuleUtil;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.config.ITenantConfig;
import neatlogic.framework.config.TenantConfigFactory;
import neatlogic.framework.dao.mapper.ConfigMapper;
import neatlogic.framework.dto.ConfigVo;
import neatlogic.framework.dto.module.ModuleGroupVo;
import neatlogic.framework.dto.module.ModuleManageSettingVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.module.tenant.dao.mapper.ModuleManageSettingMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ListTenantConfigApi extends PrivateApiComponentBase {

    @Resource
    private ConfigMapper configMapper;
    @Resource
    private ModuleManageSettingMapper moduleManageSettingMapper;

    @Override
    public String getName() {
        return "nmtat.listtenantconfigapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }
    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword"),
            @Param(name = "moduleGroup", type = ApiParamType.STRING, desc = "common.modulegroup"),
            @Param(name = "isGroupByModule", type = ApiParamType.INTEGER, desc = "是否按模块分组"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize")
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "tbodyList", explode = ConfigVo[].class, desc = "common.tbodylist")
    })
    @Description(desc = "nmtat.listtenantconfigapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        BasePageVo basePageVo = paramObj.toJavaObject(BasePageVo.class);
        List<ITenantConfig> allTenantConfigList = TenantConfigFactory.getTenantConfigList();
        Map<String, ModuleGroupVo> moduleGroupMap = ModuleUtil.getModuleGroupMap();
        filterInactiveModuleGroup(allTenantConfigList);
        String keyword = basePageVo.getKeyword();
        String moduleGroup = paramObj.getString("moduleGroup");
        if (StringUtils.isNotBlank(keyword)) {
            keyword = keyword.toLowerCase();
            for (int i = allTenantConfigList.size() - 1; i >= 0; i--) {
                ITenantConfig tenantConfig = allTenantConfigList.get(i);
                if (!isMatchKeyword(tenantConfig, moduleGroupMap, keyword)) {
                    allTenantConfigList.remove(i);
                }
            }
        }
        if (StringUtils.isNotBlank(moduleGroup)) {
            for (int i = allTenantConfigList.size() - 1; i >= 0; i--) {
                ITenantConfig tenantConfig = allTenantConfigList.get(i);
                if (!Objects.equals(tenantConfig.getModuleGroup(), moduleGroup)) {
                    allTenantConfigList.remove(i);
                }
            }
        }
        if (Objects.equals(paramObj.getInteger("isGroupByModule"), 1)) {
            return getGroupByModuleResult(allTenantConfigList, basePageVo, moduleGroupMap, getModuleGroupOrderMap());
        }
        basePageVo.setRowNum(allTenantConfigList.size());
        List<ITenantConfig> tenantConfigList = PageUtil.subList(allTenantConfigList, basePageVo);
        if (CollectionUtils.isEmpty(tenantConfigList)) {
            return TableResultUtil.getResult(new ArrayList(), basePageVo);
        }
        List<ConfigVo> tbodyList = new ArrayList<>();
        List<String> keyList = tenantConfigList.stream().map(ITenantConfig::getKey).collect(Collectors.toList());
        List<ConfigVo> configList = configMapper.getConfigListByKeyList(keyList);
        Map<String, ConfigVo> configMap = configList.stream().collect(Collectors.toMap(e -> e.getKey(), e -> e));
        for (ITenantConfig tenantConfig : tenantConfigList) {
            tbodyList.add(buildConfigVo(tenantConfig, configMap, moduleGroupMap));
        }

        // 按 key 升序排序
        tbodyList.sort(Comparator.comparing(ConfigVo::getKey));

        return TableResultUtil.getResult(tbodyList, basePageVo);
    }

    private void filterInactiveModuleGroup(List<ITenantConfig> tenantConfigList) {
        Set<String> activeModuleGroupSet = TenantContext.get().getActiveModuleGroupList().stream()
                .map(ModuleGroupVo::getGroup)
                .collect(Collectors.toSet());
        activeModuleGroupSet.add("framework");
        for (int i = tenantConfigList.size() - 1; i >= 0; i--) {
            ITenantConfig tenantConfig = tenantConfigList.get(i);
            if (!activeModuleGroupSet.contains(tenantConfig.getModuleGroup())) {
                tenantConfigList.remove(i);
            }
        }
    }

    private Object getGroupByModuleResult(List<ITenantConfig> tenantConfigList, BasePageVo basePageVo, Map<String, ModuleGroupVo> moduleGroupMap, Map<String, Integer> moduleGroupOrderMap) {
        basePageVo.setRowNum(tenantConfigList.size());
        basePageVo.setCurrentPage(1);
        basePageVo.setPageSize(Math.max(tenantConfigList.size(), 1));
        basePageVo.setPageCount(tenantConfigList.isEmpty() ? 0 : 1);
        if (CollectionUtils.isEmpty(tenantConfigList)) {
            return TableResultUtil.getResult(new ArrayList<>(), basePageVo);
        }
        List<String> keyList = tenantConfigList.stream().map(ITenantConfig::getKey).collect(Collectors.toList());
        List<ConfigVo> configList = configMapper.getConfigListByKeyList(keyList);
        Map<String, ConfigVo> configMap = configList.stream().collect(Collectors.toMap(e -> e.getKey(), e -> e));
        List<ConfigVo> tbodyList = new ArrayList<>();
        for (ITenantConfig tenantConfig : tenantConfigList) {
            tbodyList.add(buildConfigVo(tenantConfig, configMap, moduleGroupMap));
        }
        tbodyList.sort(Comparator.comparing((ConfigVo configVo) -> moduleGroupOrderMap.get(configVo.getModuleGroup()), Comparator.nullsLast(Integer::compareTo))
                .thenComparing(ConfigVo::getModuleGroupSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(ConfigVo::getModuleGroupName, Comparator.nullsLast(String::compareTo))
                .thenComparing(ConfigVo::getKey));

        Map<String, JSONObject> groupMap = new LinkedHashMap<>();
        for (ConfigVo configVo : tbodyList) {
            String moduleGroup = StringUtils.defaultIfBlank(configVo.getModuleGroup(), "unknown");
            JSONObject groupObj = groupMap.get(moduleGroup);
            if (groupObj == null) {
                groupObj = new JSONObject();
                groupObj.put("moduleGroup", moduleGroup);
                groupObj.put("moduleGroupName", StringUtils.defaultIfBlank(configVo.getModuleGroupName(), moduleGroup));
                groupObj.put("moduleGroupSort", configVo.getModuleGroupSort());
                groupObj.put("tbodyList", new ArrayList<ConfigVo>());
                groupMap.put(moduleGroup, groupObj);
            }
            List<ConfigVo> groupConfigList = (List<ConfigVo>) groupObj.get("tbodyList");
            groupConfigList.add(configVo);
            groupObj.put("configCount", groupConfigList.size());
        }
        return TableResultUtil.getResult(new ArrayList<>(groupMap.values()), basePageVo);
    }

    private Map<String, Integer> getModuleGroupOrderMap() {
        Map<String, Integer> moduleGroupOrderMap = new LinkedHashMap<>();
        ModuleManageSettingVo moduleManageSettingVo = moduleManageSettingMapper.getModuleManageSetting();
        JSONObject config = moduleManageSettingVo == null ? null : moduleManageSettingVo.getConfig();
        JSONArray groupList = config == null ? null : config.getJSONArray("groupList");
        if (CollectionUtils.isNotEmpty(groupList)) {
            for (int i = 0; i < groupList.size(); i++) {
                JSONObject groupObj = groupList.getJSONObject(i);
                String group = groupObj.getString("group");
                Integer sort = groupObj.getInteger("sort");
                if (StringUtils.isNotBlank(group) && sort != null) {
                    moduleGroupOrderMap.put(group, sort);
                }
            }
        }
        List<ModuleGroupVo> moduleGroupList = ModuleUtil.getAllModuleGroupList();
        int defaultOrderStart = moduleGroupOrderMap.values().stream().mapToInt(Integer::intValue).max().orElse(-1) + 1;
        for (int i = 0; i < moduleGroupList.size(); i++) {
            moduleGroupOrderMap.putIfAbsent(moduleGroupList.get(i).getGroup(), defaultOrderStart + i);
        }
        return moduleGroupOrderMap;
    }

    private ConfigVo buildConfigVo(ITenantConfig tenantConfig, Map<String, ConfigVo> configMap, Map<String, ModuleGroupVo> moduleGroupMap) {
        ConfigVo configVo = configMap.get(tenantConfig.getKey());
        if (configVo == null) {
            configVo = new ConfigVo();
            configVo.setKey(tenantConfig.getKey());
            configVo.setValue(tenantConfig.getValue());
        }
        configVo.setDescription(tenantConfig.getDescription());
        ApiParamType type = tenantConfig.getType();
        if (type != null) {
            configVo.setType(type.getText());
        }
        String moduleGroup = tenantConfig.getModuleGroup();
        ModuleGroupVo moduleGroupVo = StringUtils.isBlank(moduleGroup) ? null : moduleGroupMap.get(moduleGroup);
        configVo.setModuleGroup(moduleGroup);
        configVo.setModuleGroupName(moduleGroupVo == null ? moduleGroup : moduleGroupVo.getGroupName());
        configVo.setModuleGroupSort(moduleGroupVo == null ? null : moduleGroupVo.getGroupSort());
        return configVo;
    }

    private boolean isMatchKeyword(ITenantConfig tenantConfig, Map<String, ModuleGroupVo> moduleGroupMap, String keyword) {
        if (StringUtils.defaultString(tenantConfig.getKey()).toLowerCase().contains(keyword)
                || StringUtils.defaultString(tenantConfig.getDescription()).toLowerCase().contains(keyword)
                || StringUtils.defaultString(tenantConfig.getModuleGroup()).toLowerCase().contains(keyword)) {
            return true;
        }
        ModuleGroupVo moduleGroupVo = moduleGroupMap.get(tenantConfig.getModuleGroup());
        return moduleGroupVo != null && StringUtils.defaultString(moduleGroupVo.getGroupName()).toLowerCase().contains(keyword);
    }

    @Override
    public String getToken() {
        return "tenantconfig/list";
    }
}
