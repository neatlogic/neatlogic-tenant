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


package neatlogic.module.tenant.api.integration;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.INTEGRATION_MODIFY;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.crossover.IFileCrossoverService;
import neatlogic.framework.exception.file.FilePathIllegalException;
import neatlogic.framework.file.dto.AuditFilePathVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@AuthAction(action = INTEGRATION_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class IntegrationAuditDetailGetApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "integration/audit/detail/get";
    }

    @Override
    public String getName() {
        return "nmtai.integrationauditdetailgetapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "filePath", type = ApiParamType.STRING, desc = "common.filepath", isRequired = true)
    })
    @Output({
            @Param(name = "content", type = ApiParamType.STRING, desc = "common.content"),
            @Param(name = "hasMore", type = ApiParamType.BOOLEAN, desc = "common.hasmore")
    })
    @Description(desc = "nmtai.integrationauditdetailgetapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {

        String filePath = paramObj.getString("filePath");
        if (!filePath.contains("integrationaudit")) {
            throw new FilePathIllegalException(filePath);
        }
        AuditFilePathVo auditFilePathVo = new AuditFilePathVo(filePath);
        IFileCrossoverService fileCrossoverService = CrossoverServiceFactory.getApi(IFileCrossoverService.class);
        if (Objects.equals(auditFilePathVo.getServerId(), Config.SCHEDULE_SERVER_ID)) {
            return fileCrossoverService.readLocalFile(auditFilePathVo.getPath(), auditFilePathVo.getStartIndex(), auditFilePathVo.getOffset());
        } else {
            return fileCrossoverService.readRemoteFile(paramObj, auditFilePathVo.getServerId());
        }
    }
}
