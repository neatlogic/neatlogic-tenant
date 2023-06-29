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

import javax.annotation.Resource;

@Service
@Transactional
@AuthAction(action = DOCUMENTONLINE_CONFIG_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class MoveToDocumentOnlineConfigApi extends PrivateApiComponentBase {

    @Resource
    private DocumentOnlineService documentOnlineService;

    @Override
    public String getName() {
        return "将在线帮助文档从某个模块菜单移动到另一个模块菜单";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "fromModuleGroup", type = ApiParamType.STRING, isRequired = true, desc = "模块组标识"),
            @Param(name = "fromMenu", type = ApiParamType.STRING, desc = "菜单标识"),
            @Param(name = "filePath", type = ApiParamType.STRING, isRequired = true, desc = "文档路径"),
            @Param(name = "toModuleGroup", type = ApiParamType.STRING, isRequired = true, desc = "模块组标识"),
            @Param(name = "toMenu", type = ApiParamType.STRING, desc = "菜单标识"),
            @Param(name = "toAnchorPoint", type = ApiParamType.STRING, desc = "锚点")
    })
    @Output({})
    @Description(desc = "将在线帮助文档从某个模块菜单移动到另一个模块菜单")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String fromModuleGroup = paramObj.getString("fromModuleGroup");
        String fromMenu = paramObj.getString("fromMenu");
        String filePath = paramObj.getString("filePath");
        String toModuleGroup = paramObj.getString("toModuleGroup");
        String toMenu = paramObj.getString("toMenu");
        String toAnchorPoint = paramObj.getString("toAnchorPoint");

        // 根据文件路径在目录树中找到文件信息
        DocumentOnlineDirectoryVo directory = documentOnlineService.getDocumentOnlineDirectoryByFilePath(filePath);
        if (directory == null) {
            throw new DocumentOnlineNotFoundException(filePath);
        }
        DocumentOnlineConfigVo fromConfigVo = new DocumentOnlineConfigVo();
        fromConfigVo.setFilePath(filePath);
        fromConfigVo.setModuleGroup(fromModuleGroup);
        fromConfigVo.setMenu(fromMenu);
        documentOnlineService.deleteDocumentOnlineConfig(directory, fromConfigVo);
        DocumentOnlineConfigVo toConfigVo = new DocumentOnlineConfigVo();
        toConfigVo.setFilePath(filePath);
        toConfigVo.setModuleGroup(toModuleGroup);
        toConfigVo.setMenu(toMenu);
        toConfigVo.setAnchorPoint(toAnchorPoint);
        documentOnlineService.saveDocumentOnlineConfig(directory, toConfigVo);
        return null;
    }

    @Override
    public String getToken() {
        return "documentonline/config/moveto";
    }
}
