/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.tenantconfig;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.TENANT_CONFIG_BASE;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.config.ITenantConfig;
import neatlogic.framework.config.TenantConfigFactory;
import neatlogic.framework.dao.mapper.ConfigMapper;
import neatlogic.framework.dto.ConfigVo;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AuthAction(action = TENANT_CONFIG_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ListTenantConfigApi extends PrivateApiComponentBase {

    @Resource
    private ConfigMapper configMapper;

    @Override
    public String getName() {
        return "搜索当前租户配置信息列表";
    }

    @Override
    public String getConfig() {
        return null;
    }
    @Input({
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数据条目")
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "tbodyList", explode = ConfigVo[].class, desc = "配置信息列表")
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        BasePageVo basePageVo = paramObj.toJavaObject(BasePageVo.class);
        List<ITenantConfig> allTenantConfigList = TenantConfigFactory.getTenantConfigList();
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
            tbodyList.add(configVo);
        }
        return TableResultUtil.getResult(tbodyList, basePageVo);
    }

    @Override
    public String getToken() {
        return "tenantconfig/list";
    }
}
