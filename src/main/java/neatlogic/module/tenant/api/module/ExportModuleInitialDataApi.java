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

package neatlogic.module.tenant.api.module;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.MODULE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.initialdata.core.InitialDataManager;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
@AuthAction(action = MODULE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ExportModuleInitialDataApi extends PrivateBinaryStreamApiComponentBase {


    @Override
    public String getToken() {
        return "/module/data/export";
    }

    @Override
    public String getName() {
        return "导出模块初始化数据";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "moduleId", type = ApiParamType.STRING, isRequired = true, desc = "模块id")})
    @Description(desc = "导出模块初始化数据接口")
    @Override
    public Object myDoService(JSONObject jsonObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String moduleId = jsonObj.getString("moduleId");
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;fileName=\"" + moduleId + ".zip\"");
        ServletOutputStream os = response.getOutputStream();
        InitialDataManager.exportData(moduleId, os);
        return "";
    }
}
