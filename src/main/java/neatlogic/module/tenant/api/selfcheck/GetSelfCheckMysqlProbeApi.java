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
import neatlogic.framework.auth.label.SELF_CHECK_EXECUTE;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.selfcheck.annotation.SelfCheckApi;
import neatlogic.module.tenant.dao.mapper.SelfCheckMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Service
@SelfCheckApi(defaultScript = "return context.response.reachable === true;")
@AuthAction(action = SELF_CHECK_EXECUTE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetSelfCheckMysqlProbeApi extends PrivateApiComponentBase {
    @Resource
    private SelfCheckMapper selfCheckMapper;

    @Override
    public String getToken() {
        return "selfcheck/probe/mysql";
    }

    @Override
    public String getName() {
        return "MySQL连接探针";
    }

    /** 使用当前租户数据源检查 MySQL 连接，不接收外部连接信息。 */
    @Description(desc = "检查当前租户MySQL连接")
    @Output({
            @Param(name = "reachable", type = ApiParamType.BOOLEAN, desc = "是否可连接"),
            @Param(name = "durationMs", type = ApiParamType.LONG, desc = "耗时毫秒"),
            @Param(name = "status", type = ApiParamType.STRING, desc = "连接状态"),
            @Param(name = "message", type = ApiParamType.STRING, desc = "检查结果")
    })
    @Override
    public Object myDoService(JSONObject paramObj) {
        long startTime = System.currentTimeMillis();
        JSONObject result = new JSONObject();
        try {
            boolean reachable = Objects.equals(selfCheckMapper.checkMysqlConnection(), 1);
            result.put("reachable", reachable);
            if (reachable) {
                result.put("status", "UP");
                result.put("message", "连接正常");
            } else {
                result.put("status", "DOWN");
                result.put("message", "连接失败");
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
