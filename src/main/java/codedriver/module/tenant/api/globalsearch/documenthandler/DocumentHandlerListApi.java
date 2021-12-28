/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.globalsearch.documenthandler;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.ModuleVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
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
