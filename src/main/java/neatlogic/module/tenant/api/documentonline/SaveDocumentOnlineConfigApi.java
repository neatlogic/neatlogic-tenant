/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tenant.api.documentonline;

import com.alibaba.fastjson.JSONArray;
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
import neatlogic.framework.transaction.util.TransactionUtil;
import neatlogic.module.tenant.service.documentonline.DocumentOnlineService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = DOCUMENTONLINE_CONFIG_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class SaveDocumentOnlineConfigApi extends PrivateApiComponentBase {

    @Resource
    private DocumentOnlineService documentOnlineService;

    @Override
    public String getName() {
        return "nmtad.savedocumentonlineconfigapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "filePath", type = ApiParamType.STRING, isRequired = true, desc = "common.filepath"),
            @Param(name = "configList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "common.configlist")
    })
    @Output({})
    @Description(desc = "nmtad.savedocumentonlineconfigapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String filePath = paramObj.getString("filePath");
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
            TransactionStatus tx = TransactionUtil.openTx();
            try {
                // 旧的映射关系列表
                List<DocumentOnlineConfigVo> oldConfigList = directory.getConfigList();
                JSONArray configArray = paramObj.getJSONArray("configList");
                if (CollectionUtils.isEmpty(configArray)) {
                    // 遍历，删除所有旧的映射关系
                    for (DocumentOnlineConfigVo configVo : new ArrayList<>(oldConfigList)) {
                        documentOnlineService.deleteDocumentOnlineConfig(directory, configVo);
                    }
                } else {
                    // 新的映射关系列表
                    List<DocumentOnlineConfigVo> newConfigList = configArray.toJavaList(DocumentOnlineConfigVo.class);
                    for (DocumentOnlineConfigVo configVo : newConfigList) {
                        configVo.setFilePath(filePath);
                    }
                    // 需要删除的旧映射关系列表
                    List<DocumentOnlineConfigVo> needDeleteList = ListUtils.removeAll(oldConfigList, newConfigList);
                    // 遍历，删除
                    for (DocumentOnlineConfigVo configVo : needDeleteList) {
                        documentOnlineService.deleteDocumentOnlineConfig(directory, configVo);
                    }
                    // 遍历，更新所有新的映射关系
                    for (DocumentOnlineConfigVo configVo : newConfigList) {
                        documentOnlineService.saveDocumentOnlineConfig(directory, configVo);
                    }
                }
                TransactionUtil.commitTx(tx);
            } catch (Exception e) {
                directory.getConfigList().clear();
                directory.getConfigList().addAll(backupConfigList);
                TransactionUtil.rollbackTx(tx);
                throw e;
            } finally {
                TenantContext.get().setUseDefaultDatasource(false);
            }
        }
        return null;
    }

    @Override
    public String getToken() {
        return "documentonline/config/save";
    }
}
