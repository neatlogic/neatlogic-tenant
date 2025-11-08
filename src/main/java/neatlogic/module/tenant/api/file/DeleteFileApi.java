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

package neatlogic.module.tenant.api.file;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.systemuser.SystemUser;
import neatlogic.framework.exception.file.FileAccessDeniedException;
import neatlogic.framework.exception.file.FileNotFoundException;
import neatlogic.framework.exception.file.FileTypeHandlerNotFoundException;
import neatlogic.framework.exception.user.NoTenantException;
import neatlogic.framework.file.core.FileOperationType;
import neatlogic.framework.file.core.FileTypeHandlerFactory;
import neatlogic.framework.file.core.IFileTypeHandler;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
@AuthUser(SystemUser.AUTOEXEC)
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class DeleteFileApi extends PrivateApiComponentBase {

    @Resource
    private FileMapper fileMapper;

    @Override
    public String getToken() {
        return "file/delete";
    }

    @Override
    public String getName() {
        return "删除附件";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "fileId", type = ApiParamType.LONG, desc = "附件id", isRequired = true)
    })
    @Description(desc = "删除附件")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long fileId = paramObj.getLong("fileId");
        FileVo fileVo = fileMapper.getFileById(fileId);
        String tenantUuid = TenantContext.get().getTenantUuid();
        if (StringUtils.isBlank(tenantUuid)) {
            throw new NoTenantException();
        }
        if (fileVo == null) {
            throw new FileNotFoundException(fileId);
        }
        IFileTypeHandler fileTypeHandler = FileTypeHandlerFactory.getHandler(fileVo.getType());
        if (fileTypeHandler == null) {
            throw new FileTypeHandlerNotFoundException(fileVo.getType());
        }
        if (fileTypeHandler.validDeleteFile(fileVo)) {
            fileTypeHandler.deleteFile(fileVo, paramObj);
        } else {
            throw new FileAccessDeniedException(fileVo.getName(), FileOperationType.DELETE.getText());
        }
        return null;
    }
}
