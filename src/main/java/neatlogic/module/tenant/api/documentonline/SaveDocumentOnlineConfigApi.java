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
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.documentonline.crossover.IDocumentOnlineCrossoverMapper;
import neatlogic.framework.documentonline.dto.DocumentOnlineConfigVo;
import neatlogic.framework.documentonline.dto.DocumentOnlineDirectoryVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.framework.startup.DocumentOnlineInitializeIndexHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
public class SaveDocumentOnlineConfigApi extends PrivateApiComponentBase {

    @Override
    public String getName() {
        return "保存在线帮助文档与模块菜单的映射关系";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "filePath", type = ApiParamType.STRING, isRequired = true, desc = "文档路径"),
            @Param(name = "moduleGroup", type = ApiParamType.STRING, isRequired = true, desc = "模块组标识"),
            @Param(name = "menu", type = ApiParamType.STRING, isRequired = true, desc = "菜单标识"),
            @Param(name = "anchorPoint", type = ApiParamType.STRING, desc = "锚点")
    })
    @Output({})
    @Description(desc = "保存在线帮助文档与模块菜单的映射关系")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        DocumentOnlineConfigVo documentOnlineConfigVo = paramObj.toJavaObject(DocumentOnlineConfigVo.class);
        IDocumentOnlineCrossoverMapper documentOnlineCrossoverMapper = CrossoverServiceFactory.getApi(IDocumentOnlineCrossoverMapper.class);
        documentOnlineCrossoverMapper.insertDocumentOnlineConfig(documentOnlineConfigVo);
        // 根据文件路径在目录树中找到文件信息
        String filePath = documentOnlineConfigVo.getFilePath();
        String[] directoryNameList = filePath.split("/");
        DocumentOnlineDirectoryVo directory = DocumentOnlineInitializeIndexHandler.DOCUMENT_ONLINE_DIRECTORY_ROOT;
        for (int i = 1; i < directoryNameList.length; i++) {
            String directoryName  = directoryNameList[i];
            for (DocumentOnlineDirectoryVo child : directory.getChildren()) {
                String childName = child.getName();
                if (child.getIsFile()) {
                    childName += ".md";
                }
                if (Objects.equals(childName, directoryName)) {
                    directory = child;
                    break;
                }
            }
            if (directory == null) {
                break;
            }
        }
        if (directory == null) {
            return null;
        }
        // 到这里，directory代表文件信息
        List<String> ownerList = directory.getOwnerList();
        String newOwner = "moduleGroup=" + documentOnlineConfigVo.getModuleGroup();
        String menu = documentOnlineConfigVo.getMenu();
        if (StringUtils.isNotBlank(menu)) {
            newOwner += "&menu=" + menu;
        }
        // 删除旧的owner
        Iterator<String> iterator = ownerList.iterator();
        while (iterator.hasNext()) {
            String owner = iterator.next();
            if (owner.startsWith(newOwner)) {
                iterator.remove();
            }
        }
        String anchorPoint = documentOnlineConfigVo.getAnchorPoint();
        if (StringUtils.isNotBlank(anchorPoint)) {
            newOwner += "&anchorPoint=" + anchorPoint;
        }
        // 添加新的owner
        ownerList.add(newOwner);
        return null;
    }

    @Override
    public String getToken() {
        return "documentonline/config/save";
    }

}
