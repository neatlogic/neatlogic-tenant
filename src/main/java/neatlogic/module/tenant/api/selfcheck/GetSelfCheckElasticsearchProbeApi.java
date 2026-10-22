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
package neatlogic.module.tenant.api.selfcheck;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.SELF_CHECK_EXECUTE;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.selfcheck.annotation.SelfCheckApi;
import neatlogic.framework.store.elasticsearch.ElasticsearchClientFactory;
import org.springframework.stereotype.Service;

@Service
@SelfCheckApi(defaultScript = "return context.response.reachable === true;")
@AuthAction(action = SELF_CHECK_EXECUTE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetSelfCheckElasticsearchProbeApi extends PrivateApiComponentBase {
    @Override
    public String getToken() {
        return "selfcheck/probe/elasticsearch";
    }

    @Override
    public String getName() {
        return "Elasticsearch连接探针";
    }

    /** 使用平台已有 Elasticsearch 客户端读取集群健康状态。 */
    @Description(desc = "检查平台Elasticsearch连接")
    @Output({
            @Param(name = "reachable", type = ApiParamType.BOOLEAN, desc = "是否可连接"),
            @Param(name = "durationMs", type = ApiParamType.LONG, desc = "耗时毫秒"),
            @Param(name = "status", type = ApiParamType.STRING, desc = "集群健康状态"),
            @Param(name = "message", type = ApiParamType.STRING, desc = "检查结果")
    })
    @Override
    public Object myDoService(JSONObject paramObj) {
        long startTime = System.currentTimeMillis();
        JSONObject result = new JSONObject();
        try {
            ElasticsearchClient client = ElasticsearchClientFactory.getClient();
            if (client == null) {
                result.put("reachable", false);
                result.put("status", "DOWN");
                result.put("message", "客户端未配置");
            } else {
                result.put("reachable", true);
                result.put("status", client.cluster().health().status().jsonValue());
                result.put("message", "连接正常");
            }
        } catch (Exception ex) {
            result.put("reachable", false);
            result.put("status", "DOWN");
            result.put("message", "连接失败");
        }
        result.put("durationMs", System.currentTimeMillis() - startTime);
        return result;
    }
}
