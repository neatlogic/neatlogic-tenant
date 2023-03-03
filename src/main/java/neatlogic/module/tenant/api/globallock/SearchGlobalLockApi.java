/*
Copyright(c) $today.year NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. 
 */

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
