/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.integration;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.dependency.constvalue.FromType;
import codedriver.framework.dependency.core.DependencyManager;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.TableResultUtil;
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
        return "集成设置查询接口";
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
    @Description(desc = "集成设置查询接口")
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
                int count = DependencyManager.getDependencyCount(FromType.INTEGRATION, inte.getUuid());
                inte.setReferenceCount(count);
            }
        }
        return TableResultUtil.getResult(integrationList, integrationVo);
    }
}
