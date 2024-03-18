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

package neatlogic.module.tenant.api.importexport;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.file.FileNotUploadException;
import neatlogic.framework.importexport.core.ImportExportHandlerFactory;
import neatlogic.framework.importexport.dto.ImportDependencyTypeVo;
import neatlogic.framework.importexport.exception.DependencyNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
@Transactional
@OperationType(type = OperationTypeEnum.SEARCH)
public class ImportApi extends PrivateBinaryStreamApiComponentBase {

    @Override
    public String getName() {
        return "nmtai.importapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "targetType", type = ApiParamType.STRING, isRequired = true, desc = "common.importtargettype"),
            @Param(name = "userSelection", type = ApiParamType.JSONOBJECT, desc = "common.userselectionimportoption")
    })
    @Output({
            @Param(name = "typeList", explode = ImportDependencyTypeVo[].class, desc = "common.tbodylist")
    })
    @Description(desc = "nmtai.importapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        // 获取所有导入文件
        Map<String, MultipartFile> multipartFileMap = multipartRequest.getFileMap();
        // 如果没有导入文件，抛出异常
        if (multipartFileMap.isEmpty()) {
            throw new FileNotUploadException();
        }
        MultipartFile multipartFile = null;
        // 遍历导入文件
        for (Map.Entry<String, MultipartFile> entry : multipartFileMap.entrySet()) {
            multipartFile = entry.getValue();
            break;
        }
        if (multipartFile != null) {
            String targetType = paramObj.getString("targetType");
            String userSelection = paramObj.getString("userSelection");
            try {
                return ImportExportHandlerFactory.importData(multipartFile, targetType, userSelection);
            } catch (DependencyNotFoundException e) {
                JSONObject resultObj = new JSONObject();
                resultObj.put("messageList", e.getMessageList());
                return resultObj;
            }
        }
        return null;
    }

    @Override
    public String getToken() {
        return "common/import";
    }

}
