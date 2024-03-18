/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

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
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
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
