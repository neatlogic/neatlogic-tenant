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

package neatlogic.module.tenant.api.module;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.MODULE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.initialdata.core.InitialDataManager;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.binarystream.PrivateBinaryStreamApiComponentBase;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
@AuthAction(action = MODULE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ValidModuleInitialDataApi extends PrivateBinaryStreamApiComponentBase {


    @Override
    public String getToken() {
        return "/module/data/valid";
    }

    @Override
    public String getName() {
        return "校验模块初始化数据";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "file", type = ApiParamType.FILE, isRequired = true, desc = "导入文件")})
    @Output({@Param(name = "Return", explode = ValueTextVo[].class, type = ApiParamType.STRING, desc = "数据表清单")})
    @Description(desc = "校验模块初始化数据接口")
    @Override
    public Object myDoService(JSONObject jsonObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String moduleId = jsonObj.getString("moduleId");
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile multipartFile = multipartRequest.getFile("file");
        if (multipartFile != null) {
            return InitialDataManager.validData(moduleId, multipartFile.getInputStream());
        }
        return null;
    }
}
