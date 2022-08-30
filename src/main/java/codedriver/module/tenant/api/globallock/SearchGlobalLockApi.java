/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.globallock;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.globallock.GlobalLockVo;
import codedriver.framework.globallock.GlobalLockManager;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
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
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键词", xss = true),
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
