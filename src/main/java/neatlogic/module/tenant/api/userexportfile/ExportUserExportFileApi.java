/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.tenant.api.userexportfile;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.auth.label.USER_EXPORT_FILE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.FileUtil;
import neatlogic.framework.dao.mapper.UserExportFileMapper;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.userexportfile.dto.UserExportFileVo;
import neatlogic.framework.userexportfile.exception.UserExportFileDownloadDeniedException;
import neatlogic.framework.userexportfile.exception.UserExportFileNotFoundException;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.Objects;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ExportUserExportFileApi extends PrivateBinaryStreamApiComponentBase {

    @Resource
    private UserExportFileMapper userExportFileMapper;

    @Override
    public String getName() {
        return "nmtau.exportuserexportfileapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }
    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "common.id")
    })
    @Output({

    })
    @Description(desc = "nmtau.exportuserexportfileapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long id = paramObj.getLong("id");
        UserExportFileVo userExportFile = userExportFileMapper.getUserExportFileById(id);
        if (userExportFile != null) {
            if (!Objects.equals(userExportFile.getUserUuid(), UserContext.get().getUserUuid())) {
                if (!AuthActionChecker.check(USER_EXPORT_FILE_MODIFY.class)) {
                    throw new UserExportFileDownloadDeniedException(userExportFile.getName());
                }
            }
            try (InputStream in = FileUtil.getData(userExportFile.getPath())) {
                if (in != null) {
                    try (ServletOutputStream os = response.getOutputStream()) {
                        response.setContentType(userExportFile.getContentType());
                        response.setHeader("Content-Disposition", " attachment; filename=\"" + neatlogic.framework.util.FileUtil.getEncodedFileName(userExportFile.getName()) + "\"");
                        IOUtils.copyLarge(in, os);
                    }
                }
            }
        } else {
            throw new UserExportFileNotFoundException(id);
        }
        return null;
    }

    @Override
    public String getToken() {
        return "user/exportfile/export";
    }
}
