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

package neatlogic.module.tenant.api.healthcheck;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.plugin.SqlCostInterceptor;
import neatlogic.framework.healthcheck.SqlAuditManager;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class ToggleSqlInterceptorApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "/healthcheck/togglesqlinterceptor";
    }

    @Override
    public String getName() {
        return "nmtah.togglesqlinterceptorapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "action", type = ApiParamType.ENUM, isRequired = true, rule = "insert,remove,clear", desc = "insert：激活追踪，remove：取消追踪，clear：取消全部追踪"),
            @Param(name = "id", type = ApiParamType.STRING, desc = "nmtah.togglesqlinterceptorapi.input.param.desc"),
            @Param(name = "url", type = ApiParamType.STRING, desc = "nmtah.togglesqlinterceptorapi.input.param.desc")})
    @Description(desc = "nmtah.togglesqlinterceptorapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String action = jsonObj.getString("action");
        String id = jsonObj.getString("id");
        String url = jsonObj.getString("url");
        if (StringUtils.isNotBlank(action)) {
            if (action.equalsIgnoreCase("clear")) {
                // 清空时同时清理sqlId和URL两类监控配置及两类审计数据
                SqlCostInterceptor.SqlIdMap.clear();
                SqlCostInterceptor.UrlMap.clear();
                SqlAuditManager.clearSqlAudit();
                SqlAuditManager.clearRequestSqlAudit();
            } else if (action.equalsIgnoreCase("insert")) {
                // 添加监控时允许sql id和url同时填写，分别进入不同监控集合
                if (StringUtils.isNotBlank(id)) {
                    SqlCostInterceptor.SqlIdMap.addId(id);
                }
                if (StringUtils.isNotBlank(url)) {
                    SqlCostInterceptor.UrlMap.addUrl(url);
                }
            } else if (action.equalsIgnoreCase("remove")) {
                // 删除监控时分别清理sql id和url对应的历史审计记录
                if (StringUtils.isNotBlank(id)) {
                    SqlCostInterceptor.SqlIdMap.removeId(id);
                    SqlAuditManager.removeSqlAudit(id);
                }
                if (StringUtils.isNotBlank(url)) {
                    SqlCostInterceptor.UrlMap.removeUrl(url);
                    SqlAuditManager.removeRequestSqlAudit(url);
                }
            }
        }
        return null;
    }
}
