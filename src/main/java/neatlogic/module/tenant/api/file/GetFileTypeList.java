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
import neatlogic.framework.file.core.FileTypeHandlerFactory;
import neatlogic.framework.file.dto.FileTypeVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
@AuthAction(action = FILE_MODIFY.class)
public class GetFileTypeList extends PrivateApiComponentBase {


    @Override
    public String getName() {
        return "获取附件归属列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    @Output({@Param(explode = FileTypeVo[].class)})
    @Description(desc = "获取附件归属列表接口")
    public Object myDoService(JSONObject paramObj) throws Exception {
        return FileTypeHandlerFactory.getActiveFileTypeHandler();
    }

    @Override
    public String getToken() {
        return "file/type/list";
    }
}
