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
