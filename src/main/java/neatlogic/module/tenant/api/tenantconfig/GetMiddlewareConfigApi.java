/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.tenant.api.tenantconfig;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.dao.mapper.DatasourceMapper;
import neatlogic.framework.dao.mapper.ElasticsearchMapper;
import neatlogic.framework.dao.mapper.MongoDbMapper;
import neatlogic.framework.dto.DatasourceVo;
import neatlogic.framework.dto.ElasticsearchVo;
import neatlogic.framework.dto.MongoDbVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetMiddlewareConfigApi extends PrivateApiComponentBase {

    @Resource
    private MongoDbMapper mongoDbMapper;

    @Resource
    private ElasticsearchMapper elasticsearchMapper;

    @Resource
    private DatasourceMapper datasourceMapper;

    @Override
    public String getName() {
        return "nmtat.getmiddlewareconfigapi.getname";
    }

    @Input({})
    @Output({})
    @Description(desc = "nmtat.getmiddlewareconfigapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        try {
            String tenantUuid = TenantContext.get().getTenantUuid();
            TenantContext.get().setUseMasterDatabase(true);
            DatasourceVo datasourceVo = datasourceMapper.getDatasourceByTenantUuid(tenantUuid);
            if (datasourceVo != null) {
                JSONObject datasourceObj = new JSONObject(true);
                datasourceObj.put("driverClassName", datasourceVo.getDriver());
                String url = datasourceVo.getUrl();
                if (StringUtils.isNotBlank(datasourceVo.getHost())) {
                    url = url.replace("{host}", datasourceVo.getHost());
                }
                if (datasourceVo.getPort() != null) {
                    url = url.replace("{port}", datasourceVo.getPort().toString());
                }
                url = url.replace("{dbname}", "neatlogic_" + tenantUuid);
                datasourceObj.put("jdbcUrl", url);
                datasourceObj.put("username", datasourceVo.getUsername());
                datasourceObj.put("password", "*".repeat(datasourceVo.getPasswordPlain().length()));
                resultObj.put("datasource", datasourceObj);
            }
            MongoDbVo mongoDbVo = mongoDbMapper.getTenantMongoDbByTenantUuid(tenantUuid);
            if (mongoDbVo != null) {
                JSONObject mongoDbObj = new JSONObject();
                String url = "mongodb://"
                        + mongoDbVo.getUsername() + ":" + "*".repeat(mongoDbVo.getPasswordPlain().length())
                        + "@" + mongoDbVo.getHost() + "/" + mongoDbVo.getDatabase() + "?" + mongoDbVo.getOption();
                mongoDbObj.put("url", url);
                resultObj.put("mongodb", mongoDbObj);
            }
            ElasticsearchVo elasticsearchVo = elasticsearchMapper.getTenantElasticsearchByTenantUuid(tenantUuid);
            if (elasticsearchVo != null) {
                JSONObject elasticsearchObj = new JSONObject(true);
                elasticsearchObj.put("host", elasticsearchVo.getHost());
                elasticsearchObj.put("username", elasticsearchVo.getUsername());
                elasticsearchObj.put("password", "*".repeat(elasticsearchVo.getPasswordPlain().length()));
                elasticsearchObj.put("config", elasticsearchVo.getConfig());
                resultObj.put("elasticsearch", elasticsearchObj);
            }
        } finally {
            TenantContext.get().setUseMasterDatabase(false);
        }
        return resultObj;
    }


    @Override
    public String getToken() {
        return "middlewareconfig/get";
    }
}
