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
