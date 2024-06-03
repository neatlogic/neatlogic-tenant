/*
 * Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neatlogic.module.tenant.api.dynamicplugin;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.dynamicplugin.crossover.IDynamicPluginCrossoverMapper;
import neatlogic.framework.dynamicplugin.crossover.IDynamicPluginCrossoverService;
import neatlogic.framework.dynamicplugin.dto.DynamicPluginVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ListDynamicPluginApi extends PrivateApiComponentBase {

    @Override
    public String getName() {
        return "获取动态插件列表";
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, xss = true, desc = "common.keyword"),
            @Param(name = "defaultValue", type = ApiParamType.JSONARRAY, desc = "common.defaultvalue"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize")
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "tbodyList", explode = DynamicPluginVo[].class, desc = "common.tbodylist")
    })
    @Description(desc = "获取动态插件")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        IDynamicPluginCrossoverMapper dynamicPluginCrossoverMapper = CrossoverServiceFactory.getApi(IDynamicPluginCrossoverMapper.class);
        BasePageVo search = paramObj.toJavaObject(BasePageVo.class);
        JSONArray defaultValue = search.getDefaultValue();
        if (CollectionUtils.isNotEmpty(defaultValue)) {
            List<Long> idList = defaultValue.toJavaList(Long.class);
            List<DynamicPluginVo> tbodyList = dynamicPluginCrossoverMapper.getDynamicPluginListByIdList(idList);
            return TableResultUtil.getResult(tbodyList);
        }
        IDynamicPluginCrossoverService dynamicPluginCrossoverService = CrossoverServiceFactory.getApi(IDynamicPluginCrossoverService.class);
        List<DynamicPluginVo> tbodyList = dynamicPluginCrossoverService.searchDynamicPluginList(search);
        return TableResultUtil.getResult(tbodyList, search);
    }

    @Override
    public String getToken() {
        return "dynamicplugin/list";
    }
}
