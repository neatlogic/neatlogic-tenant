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

package neatlogic.module.tenant.api.globalsearch.documenthandler;

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.module.ModuleVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

//@Service
//@OperationType(type = OperationTypeEnum.SEARCH)
public class DocumentHandlerListApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "globalsearch/documenthandler/list";
    }

    @Override
    public String getName() {
        return "全局搜索文档处理器获取接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({@Param(name = "type", type = ApiParamType.STRING, desc = "处理器类型"), @Param(name = "name", type = ApiParamType.STRING, desc = "处理器名称"), @Param(name = "documentCount", type = ApiParamType.INTEGER, desc = "文档数量"), @Param(name = "rebuildAudit", type = ApiParamType.JSONARRAY, desc = "重建日志操作记录")})
    @Description(desc = "全局搜索文档处理器获取接口")
    @Override
    public Object myDoService(JSONObject jsonObj) {
        TenantContext tenantContext = TenantContext.get();
        List<ModuleVo> moduleList = tenantContext.getActiveModuleList();
        //List<DocumentHandlerVo> documentHandlerList = DocumentHandlerFactory.getDocumentHandlerList(moduleList);
        //List<RebuildAuditVo> rebuildAuditList = globalSearchService.getAllRebuildAudit();
		/*for (DocumentHandlerVo handler : documentHandlerList) {
			handler.setDocumentCount(globalSearchService.getDocumentCountByType(handler.getType()));
			for (RebuildAuditVo audit : rebuildAuditList) {
				if (handler.getType().equals(audit.getType())) {
					handler.setRebuildAudit(audit);
					break;
				}
			}
		}*/
        return null;//documentHandlerList;
    }

}
