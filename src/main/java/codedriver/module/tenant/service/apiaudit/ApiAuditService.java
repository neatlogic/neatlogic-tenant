package codedriver.module.tenant.service.apiaudit;

import codedriver.framework.restful.dto.ApiAuditVo;

import java.util.List;

public interface ApiAuditService {

    public List<ApiAuditVo> searchApiAuditVo(ApiAuditVo vo) throws ClassNotFoundException;
    public List<ApiAuditVo> searchApiAuditForExport(ApiAuditVo vo) throws ClassNotFoundException;
}
