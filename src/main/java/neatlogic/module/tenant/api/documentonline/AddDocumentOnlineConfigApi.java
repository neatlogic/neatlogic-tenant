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

package neatlogic.module.tenant.api.documentonline;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.DOCUMENTONLINE_CONFIG_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.documentonline.dto.DocumentOnlineConfigVo;
import neatlogic.framework.documentonline.dto.DocumentOnlineDirectoryVo;
import neatlogic.framework.documentonline.exception.DocumentOnlineNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.service.documentonline.DocumentOnlineService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = DOCUMENTONLINE_CONFIG_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class AddDocumentOnlineConfigApi extends PrivateApiComponentBase {

    @Resource
    private DocumentOnlineService documentOnlineService;

    @Override
    public String getName() {
        return "nmtad.adddocumentonlineconfigapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "filePath", type = ApiParamType.STRING, isRequired = true, desc = "common.filepath"),
            @Param(name = "moduleGroup", type = ApiParamType.STRING, isRequired = true, desc = "common.modulegroup"),
            @Param(name = "menu", type = ApiParamType.STRING, desc = "common.menu"),
            @Param(name = "anchorPoint", type = ApiParamType.STRING, desc = "common.anchorpoint")
    })
    @Output({})
    @Description(desc = "nmtad.adddocumentonlineconfigapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        DocumentOnlineConfigVo documentOnlineConfigVo = paramObj.toJavaObject(DocumentOnlineConfigVo.class);
        String filePath = documentOnlineConfigVo.getFilePath();
        // 根据文件路径在目录树中找到文件信息
        DocumentOnlineDirectoryVo directory = documentOnlineService.getDocumentOnlineDirectoryByFilePath(filePath);
        if (directory == null) {
            throw new DocumentOnlineNotFoundException(filePath);
        }
        synchronized (directory) {
            // 操作前备份configList，如果发生异常，事务回滚，不改变configList
            List<DocumentOnlineConfigVo> backupConfigList = new ArrayList<>();
            for (DocumentOnlineConfigVo configVo : directory.getConfigList()) {
                backupConfigList.add(new DocumentOnlineConfigVo(configVo));
            }
            TenantContext.get().setUseDefaultDatasource(true);
            try {
                documentOnlineService.saveDocumentOnlineConfig(directory, documentOnlineConfigVo);
            } catch (Exception e) {
                directory.getConfigList().clear();
                directory.getConfigList().addAll(backupConfigList);
                throw e;
            } finally {
                TenantContext.get().setUseDefaultDatasource(false);
            }
        }
        return null;
    }

    @Override
    public String getToken() {
        return "documentonline/config/add";
    }

}
