package codedriver.module.tenant.service.apiaudit;

import codedriver.framework.restful.dto.ApiAuditVo;
import codedriver.framework.restful.dto.ApiVo;

import java.util.List;

public interface ApiAuditService {

    public List<ApiAuditVo> searchApiAuditVo(ApiAuditVo vo) throws ClassNotFoundException;
    public List<ApiAuditVo> searchApiAuditForExport(ApiAuditVo vo) throws ClassNotFoundException;
    public List<ApiVo> getApiListForTree();
}
