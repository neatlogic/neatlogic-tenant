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
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.FILE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.file.FileTypeHandlerNotFoundException;
import neatlogic.framework.file.core.FileTypeHandlerFactory;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileTypeVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = FILE_MODIFY.class)
public class DeleteFileTypeConfigApi extends PrivateApiComponentBase {

    @Resource
    private FileMapper fileMapper;

    @Override
    public String getToken() {
        return "file/type/config/delete";
    }

    @Override
    public String getName() {
        return "删除附件类型规则";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "附件类型")})
    @Description(desc = "删除附件类型规则接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String name = jsonObj.getString("name");
        FileTypeVo activeFileTypeVo = FileTypeHandlerFactory.getActiveFileTypeHandlerByType(name);
        if (activeFileTypeVo == null) {
            throw new FileTypeHandlerNotFoundException(name);
        }
        fileMapper.deleteFileTypeConfig(activeFileTypeVo.getName());
        return null;
    }
}
