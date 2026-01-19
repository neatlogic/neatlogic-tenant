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
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.crossover.IUserExportFileCrossoverMapper;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.userexportfile.dto.UserExportFileVo;
import neatlogic.framework.userexportfile.exception.UserExportFileDeleteDeniedException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class DeleteUserExportFileApi extends PrivateApiComponentBase {

    @Override
    public String getName() {
        return "nmtau.deleteuserexportfileapi.getname";
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "common.id")
    })
    @Output({

    })
    @Description(desc = "nmtau.deleteuserexportfileapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long id = paramObj.getLong("id");
        IUserExportFileCrossoverMapper userExportFileCrossoverMapper = CrossoverServiceFactory.getApi(IUserExportFileCrossoverMapper.class);
        UserExportFileVo userExportFile = userExportFileCrossoverMapper.getUserExportFileById(id);
        if (userExportFile != null) {
            if (!Objects.equals(userExportFile.getUserUuid(), UserContext.get().getUserUuid())) {
                if (!AuthActionChecker.check(USER_EXPORT_FILE_MODIFY.class)) {
                    throw new UserExportFileDeleteDeniedException(userExportFile.getName());
                }
            }
            userExportFileCrossoverMapper.deleteUserExportFileById(id);
            FileUtil.deleteData(userExportFile.getPath());
        }
        return null;
    }

    @Override
    public String getToken() {
        return "user/exportfile/delete";
    }
}
