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

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.config.ITenantConfig;
import neatlogic.framework.config.TenantConfigFactory;
import neatlogic.framework.dao.mapper.ConfigMapper;
import neatlogic.framework.dto.ConfigVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ListTenantConfigApi extends PrivateApiComponentBase {

    @Resource
    private ConfigMapper configMapper;

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
        String keyword = basePageVo.getKeyword();
        if (StringUtils.isNotBlank(keyword)) {
            keyword = keyword.toLowerCase();
            for (int i = allTenantConfigList.size() - 1; i >= 0; i--) {
                ITenantConfig tenantConfig = allTenantConfigList.get(i);
                if (!tenantConfig.getKey().toLowerCase().contains(keyword)
                        && !tenantConfig.getDescription().toLowerCase().contains(keyword)) {
                    allTenantConfigList.remove(i);
                }
            }
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
            ConfigVo configVo = configMap.get(tenantConfig.getKey());
            if (configVo == null) {
                configVo = new ConfigVo();
                configVo.setKey(tenantConfig.getKey());
                configVo.setValue(tenantConfig.getValue());
            }
            configVo.setDescription(tenantConfig.getDescription());
            ApiParamType type = tenantConfig.getType();
            if (type != null) {
                configVo.setType(type.getValue());
                if (type == ApiParamType.PASSWORD && StringUtils.isNotBlank(configVo.getValue())) {
                    configVo.setValue("******");
                }
            }
            tbodyList.add(configVo);
        }

        // 按 key 升序排序
        tbodyList.sort(Comparator.comparing(ConfigVo::getKey));

        return TableResultUtil.getResult(tbodyList, basePageVo);
    }

    @Override
    public String getToken() {
        return "tenantconfig/list";
    }
}
