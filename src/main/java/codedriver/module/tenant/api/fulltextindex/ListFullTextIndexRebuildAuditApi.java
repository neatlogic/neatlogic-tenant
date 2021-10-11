/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.fulltextindex;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.fulltextindex.core.FullTextIndexHandlerFactory;
import codedriver.framework.fulltextindex.dao.mapper.FullTextIndexMapper;
import codedriver.framework.fulltextindex.dao.mapper.FullTextIndexRebuildAuditMapper;
import codedriver.framework.fulltextindex.dto.fulltextindex.FullTextIndexRebuildAuditVo;
import codedriver.framework.fulltextindex.dto.fulltextindex.FullTextIndexTypeVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ListFullTextIndexRebuildAuditApi extends PrivateApiComponentBase {
    @Resource
    private FullTextIndexRebuildAuditMapper fullTextIndexRebuildAuditMapper;

    @Resource
    private FullTextIndexMapper fullTextIndexMapper;

    @Override
    public String getToken() {
        return "fulltextindex/rebuildaudit/list";
    }

    @Override
    public String getName() {
        return "获取索引重建记录列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "typeList", type = ApiParamType.JSONARRAY, desc = "类型列表")})
    @Output({@Param(explode = FullTextIndexRebuildAuditVo[].class)})
    @Description(desc = "获取索引重建记录列表接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        FullTextIndexRebuildAuditVo audit = JSONObject.toJavaObject(paramObj, FullTextIndexRebuildAuditVo.class);
        List<FullTextIndexRebuildAuditVo> rebuildAuditList = fullTextIndexRebuildAuditMapper.searchFullTextIndexRebuildAudit(audit);
        List<FullTextIndexTypeVo> fullTextIndexTypeList = FullTextIndexHandlerFactory.getAllTypeList();
        for (FullTextIndexTypeVo typeVo : fullTextIndexTypeList) {
            Optional<FullTextIndexRebuildAuditVo> op = rebuildAuditList.stream().filter(d -> d.getType().equals(typeVo.getType())).findFirst();
            if (!op.isPresent()) {
                FullTextIndexRebuildAuditVo auditVo = new FullTextIndexRebuildAuditVo();
                auditVo.setType(typeVo.getType());
                auditVo.setTypeName(typeVo.getTypeName());
                auditVo.setIndexCount(0);
                rebuildAuditList.add(auditVo);
            } else {
                op.get().setTypeName(typeVo.getTypeName());
                op.get().setIndexCount(fullTextIndexMapper.getFullTextIndexCountByType(typeVo));
            }
        }
        return rebuildAuditList;
    }
}
