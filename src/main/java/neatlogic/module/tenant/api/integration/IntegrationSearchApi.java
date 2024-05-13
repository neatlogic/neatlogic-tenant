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

package neatlogic.module.tenant.api.integration;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.ParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.dependency.constvalue.FrameworkFromType;
import neatlogic.framework.dependency.core.DependencyManager;
import neatlogic.framework.integration.dao.mapper.IntegrationMapper;
import neatlogic.framework.integration.dto.IntegrationVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class IntegrationSearchApi extends PrivateApiComponentBase {

    @Resource
    private IntegrationMapper integrationMapper;

    @Override
    public String getToken() {
        return "integration/search";
    }

    @Override
    public String getName() {
        return "nmtai.integrationsearchapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字"),
            @Param(name = "defaultValue", type = ApiParamType.JSONARRAY, desc = "回显值"),
            @Param(name = "handler", type = ApiParamType.STRING, desc = "组件"),
            @Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", desc = "启用"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数量")})
    @Output({@Param(explode = BasePageVo.class), @Param(name = "integrationList", explode = IntegrationVo[].class, desc = "集成设置列表")})
    @Description(desc = "nmtai.integrationsearchapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<IntegrationVo> integrationList;
        IntegrationVo integrationVo = JSON.toJavaObject(jsonObj, IntegrationVo.class);
        JSONArray defaultValue = integrationVo.getDefaultValue();
        if (CollectionUtils.isNotEmpty(defaultValue)) {
            List<String> uuidList = defaultValue.toJavaList(String.class);
            integrationList = integrationMapper.getIntegrationListByUuidList(uuidList);
        } else {
            integrationList = integrationMapper.searchIntegration(integrationVo);
            if (!integrationList.isEmpty()) {
                int rowNum = integrationMapper.searchIntegrationCount(integrationVo);
                integrationVo.setRowNum(rowNum);
            }
        }
        //补充类型对应表达式信息
        if (CollectionUtils.isNotEmpty(integrationList)) {
            for (IntegrationVo inte : integrationList) {
                JSONObject paramJson = inte.getConfig().getJSONObject("param");
                if (paramJson != null) {
                    JSONArray paramList = paramJson.getJSONArray("paramList");
                    if (CollectionUtils.isNotEmpty(paramList)) {
                        for (Object paramObj : paramList) {
                            JSONObject param = (JSONObject) paramObj;
                            //设置typeName
                            String type = param.getString("type");
                            if (StringUtils.isNotBlank(type)) {
                                ParamType pt = ParamType.getParamType(type);
                                if (pt != null) {
                                    //增加参数回显模版-赖文韬-202006291121
                                    String freemarkerTemplate = pt.getFreemarkerTemplate(param.getString("name"));
                                    param.put("freemarkerTemplate", freemarkerTemplate);
                                    param.put("expresstionList", pt.getExpressionJSONArray());
                                    param.put("typeName", Objects.requireNonNull(pt).getText());
                                }
                            }
                        }
                    }
                }
                int count = DependencyManager.getDependencyCount(FrameworkFromType.INTEGRATION, inte.getUuid());
                inte.setReferenceCount(count);
            }
        }
        return TableResultUtil.getResult(integrationList, integrationVo);
    }
}
