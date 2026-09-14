/*Copyright (C) 2023  深圳极向量科技有限公司 All Rights Reserved.

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

package neatlogic.module.tenant.api.globallock;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.globallock.GlobalLockVo;
import neatlogic.framework.globallock.GlobalLockManager;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchGlobalLockApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "global/lock/search";
    }

    @Override
    public String getName() {
        return "globallock.search";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keywordParam", type = ApiParamType.JSONOBJECT, desc = "globallock.filters", help = "globallock.filtershelp", xss = true),
            @Param(name = "handler", type = ApiParamType.STRING, desc = "globallock.handler", xss = true),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "globallock.currentpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "globallock.pagesize")
    })
    @Output({
            @Param(name = "tbodyList", explode = GlobalLockVo.class, desc = "globallock.locks"),
    })
    @Description(desc = "globallock.search")
    /** 校验请求并执行当前租户范围内的锁操作。 */
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        GlobalLockVo globalLockVo = JSON.toJavaObject(jsonObj, GlobalLockVo.class);
        return GlobalLockManager.searchGlobalLock(globalLockVo);
    }
}
