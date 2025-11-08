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

package neatlogic.module.tenant.api.globalsearch.documenthandler;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.module.ModuleVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

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
