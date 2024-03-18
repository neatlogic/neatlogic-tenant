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
