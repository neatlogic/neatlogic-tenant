package codedriver.module.tenant.service.apiaudit;

import codedriver.framework.restful.dto.ApiAuditVo;

import java.util.List;
import java.util.Map;

public interface ApiAuditService {

    public List<ApiAuditVo> searchApiAuditVo(ApiAuditVo vo);
    public List<ApiAuditVo> searchApiAuditForExport(ApiAuditVo vo);
}
