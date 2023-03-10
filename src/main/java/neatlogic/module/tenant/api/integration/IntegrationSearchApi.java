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

package neatlogic.module.tenant.api.integration;

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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class IntegrationSearchApi extends PrivateApiComponentBase {

    @Autowired
    private IntegrationMapper integrationMapper;

    @Override
    public String getToken() {
        return "integration/search";
    }

    @Override
    public String getName() {
        return "????????????????????????";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "?????????"),
            @Param(name = "defaultValue", type = ApiParamType.JSONARRAY, desc = "?????????"),
            @Param(name = "handler", type = ApiParamType.STRING, desc = "??????"),
            @Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", desc = "??????"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "?????????"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "????????????")})
    @Output({@Param(explode = BasePageVo.class), @Param(name = "integrationList", explode = IntegrationVo[].class, desc = "??????????????????")})
    @Description(desc = "????????????????????????")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<IntegrationVo> integrationList = new ArrayList<>();
        IntegrationVo integrationVo = JSONObject.toJavaObject(jsonObj, IntegrationVo.class);
        JSONArray defaultValue = integrationVo.getDefaultValue();
        if (CollectionUtils.isNotEmpty(defaultValue)) {
            List<String> uuidList = defaultValue.toJavaList(String.class);
            integrationList = integrationMapper.getIntegrationListByUuidList(uuidList);
        } else {
            integrationList = integrationMapper.searchIntegration(integrationVo);
            if (integrationList.size() > 0) {
                int rowNum = integrationMapper.searchIntegrationCount(integrationVo);
                integrationVo.setRowNum(rowNum);
            }
        }
        //?????????????????????????????????
        if (CollectionUtils.isNotEmpty(integrationList)) {
            for (IntegrationVo inte : integrationList) {
                JSONObject paramJson = inte.getConfig().getJSONObject("param");
                if (paramJson != null) {
                    JSONArray paramList = paramJson.getJSONArray("paramList");
                    if (CollectionUtils.isNotEmpty(paramList)) {
                        for (Object paramObj : paramList) {
                            JSONObject param = (JSONObject) paramObj;
                            //??????typeName
                            String type = param.getString("type");
                            if (StringUtils.isNotBlank(type)) {
                                ParamType pt = ParamType.getParamType(type);
                                if (pt != null) {
                                    //????????????????????????-?????????-202006291121
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
