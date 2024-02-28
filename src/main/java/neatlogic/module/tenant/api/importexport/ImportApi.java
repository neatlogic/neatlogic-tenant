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
