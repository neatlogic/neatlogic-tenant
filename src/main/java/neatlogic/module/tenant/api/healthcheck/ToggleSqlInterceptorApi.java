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

package neatlogic.module.tenant.api.healthcheck;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.plugin.SqlCostInterceptor;
import neatlogic.framework.healthcheck.SqlAuditManager;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service

@OperationType(type = OperationTypeEnum.OPERATE)
public class ToggleSqlInterceptorApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "/healthcheck/togglesqlinterceptor";
    }

    @Override
    public String getName() {
        return "控制系统SQL追踪";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "action", type = ApiParamType.ENUM, isRequired = true, rule = "insert,remove,clear", desc = "insert：激活追踪指定SQL，remove：取消追踪指定SQL，clear：取消追踪所有SQL"),
            @Param(name = "id", type = ApiParamType.STRING, desc = "mapper配置文件中的sql id")})
    @Description(desc = "打开指定SQL日志，在标准输出中能查看最终执行SQL和执行时间")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String action = jsonObj.getString("action");
        String id = jsonObj.getString("id");
        if (StringUtils.isNotBlank(action)) {
            if (action.equalsIgnoreCase("clear")) {
                SqlCostInterceptor.SqlIdMap.clear();
                SqlAuditManager.clearSqlAudit();
            } else if (action.equalsIgnoreCase("insert") && StringUtils.isNotBlank(id)) {
                SqlCostInterceptor.SqlIdMap.addId(id);
            } else if (action.equalsIgnoreCase("remove") && StringUtils.isNotBlank(id)) {
                SqlCostInterceptor.SqlIdMap.removeId(id);
                SqlAuditManager.removeSqlAudit(id);
            }
        }
        return null;
    }
}
