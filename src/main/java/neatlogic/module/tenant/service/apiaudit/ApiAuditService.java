package neatlogic.module.tenant.service.apiaudit;

import neatlogic.framework.restful.dto.ApiAuditVo;
import neatlogic.framework.restful.dto.ApiVo;

import java.io.OutputStream;
import java.util.List;

public interface ApiAuditService {

    public List<ApiAuditVo> searchApiAuditVo(ApiAuditVo vo) throws ClassNotFoundException;
    public int searchApiAuditVoCount(ApiAuditVo vo) throws ClassNotFoundException;
    public void exportApiAudit(ApiAuditVo vo, OutputStream stream) throws Exception;
    public List<ApiVo> getApiListForTree();
}
