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

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.globallock.GlobalLockVo;
import neatlogic.framework.globallock.GlobalLockManager;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchGlobalLockApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "global/lock/search";
    }

    @Override
    public String getName() {
        return "搜索全局锁";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keywordParam", type = ApiParamType.JSONOBJECT, desc = "过滤关键词", xss = true),
            @Param(name = "handler", type = ApiParamType.STRING, desc = "处理器", xss = true),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数据条目")
    })
    @Output({
            @Param(name = "tbodyList", explode = GlobalLockVo.class, desc = "锁i"),
    })
    @Description(desc = "搜索全局锁接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        GlobalLockVo globalLockVo = JSONObject.toJavaObject(jsonObj, GlobalLockVo.class);
        return GlobalLockManager.searchGlobalLock(globalLockVo);
    }
}
