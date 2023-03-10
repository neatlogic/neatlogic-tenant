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

package neatlogic.module.tenant.api.integration;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.file.FilePathIllegalException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.AuditUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class IntegrationAuditDetailGetApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "integration/audit/detail/get";
    }

    @Override
    public String getName() {
        return "获取集成管理审计内容";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "filePath", type = ApiParamType.STRING, desc = "调用记录文件路径", isRequired = true)})
    @Output({@Param(type = ApiParamType.STRING)})
    @Description(desc = "获取集成管理审计内容")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {

        JSONObject resultJson = new JSONObject();

        String filePath = jsonObj.getString("filePath");

        if (!filePath.contains("?") || !filePath.contains("&") || !filePath.contains("=")) {
            throw new FilePathIllegalException("文件路径格式错误");
        }

        long offset = Long.parseLong(filePath.split("\\?")[1].split("&")[1].split("=")[1]);

        String result;
        if (offset > AuditUtil.maxFileSize) {
            result = AuditUtil.getAuditDetail(filePath);
            resultJson.put("result", result);
            resultJson.put("hasMore", true);
        } else {
            result = AuditUtil.getAuditDetail(filePath);
            resultJson.put("result", result);
            resultJson.put("hasMore", false);
        }
        return resultJson;
    }
}
