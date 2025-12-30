/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.tenant.api.datawarehouse;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.DATA_WAREHOUSE_BASE;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.datawarehouse.dao.mapper.DatabaseMapper;
import neatlogic.framework.datawarehouse.dto.DatabaseVo;
import neatlogic.framework.integration.dao.mapper.IntegrationMapper;
import neatlogic.framework.integration.dto.IntegrationVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.$;
import neatlogic.module.framework.datawarehouse.integration.handler.DataWareHouseIntegrationHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = DATA_WAREHOUSE_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchDataSourceDbTypeApi extends PrivateApiComponentBase {

    private final String MYSQL = "mysql";
    private final String MONGODB = "mongodb";
    private final String ELASTICSEARCH = "elasticsearch";
    @Resource
    private DatabaseMapper databaseMapper;

    @Resource
    private IntegrationMapper integrationMapper;

    @Override
    public String getName() {
        return "nmtad.searchdatasourcedbtypeapi.getname";
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword")
    })
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "common.tbodylist")
    })
    @Description(desc = "nmtad.searchdatasourcedbtypeapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String keyword = paramObj.getString("keyword");
        JSONObject resultObj = new JSONObject();
        JSONArray tbodyList = new JSONArray();
        JSONArray mysqlChildren = new JSONArray();
        if (StringUtils.isBlank(keyword) || MYSQL.contains(keyword.toLowerCase())) {
            mysqlChildren.fluentAdd(new JSONObject().fluentPut("value", MYSQL).fluentPut("text", MYSQL));
        }
        tbodyList.add(new JSONObject()
                .fluentPut("value", MYSQL.toUpperCase()).fluentPut("text", MYSQL)
                .fluentPut("children", mysqlChildren )
        );
        JSONArray mongodbChildren = new JSONArray();
        if (StringUtils.isBlank(keyword) || MONGODB.contains(keyword.toLowerCase())) {
            mongodbChildren.fluentAdd(new JSONObject().fluentPut("value", MONGODB).fluentPut("text", MONGODB));
        }
        tbodyList.add(new JSONObject()
                .fluentPut("value", MONGODB.toUpperCase()).fluentPut("text", MONGODB)
                .fluentPut("children", mongodbChildren)
        );
        JSONArray elasticsearchChildren = new JSONArray();
        if (StringUtils.isBlank(keyword) || ELASTICSEARCH.contains(keyword.toLowerCase())) {
            elasticsearchChildren.fluentAdd(new JSONObject().fluentPut("value", ELASTICSEARCH).fluentPut("text", ELASTICSEARCH));
        }
        tbodyList.add(new JSONObject()
                .fluentPut("value", ELASTICSEARCH.toUpperCase()).fluentPut("text", ELASTICSEARCH)
                .fluentPut("children", elasticsearchChildren)
        );
        JSONArray databaseChildren = new JSONArray();
        DatabaseVo searchVo = new DatabaseVo();
        searchVo.setKeyword(keyword);
        searchVo.setCurrentPage(1);
        searchVo.setPageSize(500);
        List<DatabaseVo> dataBaseList = databaseMapper.getDataBaseList(searchVo);
        for (DatabaseVo databaseVo : dataBaseList) {
            databaseChildren.add(new JSONObject().fluentPut("value", databaseVo.getType() + "-" + databaseVo.getId()).fluentPut("text", databaseVo.getName()));
        }
        tbodyList.add(new JSONObject()
                .fluentPut("value", "database").fluentPut("text", $.t("common.datasource"))
                .fluentPut("children", databaseChildren));

        JSONArray integrationChildren = new JSONArray();
        IntegrationVo integrationVo = new IntegrationVo();
        integrationVo.setKeyword(keyword);
        integrationVo.setCurrentPage(1);
        integrationVo.setPageSize(500);
        integrationVo.setHandler(DataWareHouseIntegrationHandler.class.getSimpleName());
        integrationVo.setIsActive(1);
        List<IntegrationVo> integrationList = integrationMapper.searchIntegration(integrationVo);
        for (IntegrationVo integration : integrationList) {
            integrationChildren.add(new JSONObject().fluentPut("value", "integration-" + integration.getUuid()).fluentPut("text", integration.getName()));
        }
        tbodyList.add(new JSONObject()
                .fluentPut("value", "integration").fluentPut("text", $.t("term.framework.integration"))
                .fluentPut("children", integrationChildren));
        resultObj.put("tbodyList", tbodyList);
        return resultObj;
    }


    @Override
    public String getToken() {
        return "datawarehouse/datasource/dbtype/search";
    }
}
