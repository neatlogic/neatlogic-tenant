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

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.SELF_CHECK_VIEW;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.dto.selfcheck.SelfCheckRunVo;
import neatlogic.module.tenant.service.selfcheck.SelfCheckService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = SELF_CHECK_VIEW.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchSelfCheckRunApi extends PrivateApiComponentBase {
    @Resource
    private SelfCheckService selfCheckService;

    /** 返回 API 标识。 */
    @Override
    public String getToken() {
        return "selfcheck/run/search";
    }

    /** 返回 API 名称。 */
    @Override
    public String getName() {
        return "查询功能自检执行记录";
    }

    /** 分页查询自检执行记录。 */
    @Input({
            @Param(name = "scenarioId", type = ApiParamType.LONG, desc = "场景ID"),
            @Param(name = "status", type = ApiParamType.ENUM, rule = "PENDING,RUNNING,SUCCEED,FAILED,ABORTED", desc = "状态"),
            @Param(name = "keyword", type = ApiParamType.STRING, xss = true, desc = "场景关键字"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数量")
    })
    @Description(desc = "分页查询功能自检执行记录")
    @Override
    public Object myDoService(JSONObject paramObj) {
        return selfCheckService.searchRun(paramObj.toJavaObject(SelfCheckRunVo.class));
    }
}
