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

package neatlogic.module.tenant.api.fulltextindex;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.FULLTEXTINDEX_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.fulltextindex.core.FullTextIndexHandlerFactory;
import neatlogic.framework.fulltextindex.dao.mapper.FullTextIndexMapper;
import neatlogic.framework.fulltextindex.dao.mapper.FullTextIndexRebuildAuditMapper;
import neatlogic.framework.fulltextindex.dto.fulltextindex.FullTextIndexRebuildAuditVo;
import neatlogic.framework.fulltextindex.dto.fulltextindex.FullTextIndexTypeVo;
import neatlogic.framework.fulltextindex.enums.FullTextIndexHandlerType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.store.elasticsearch.ElasticsearchIndexFactory;
import neatlogic.framework.store.elasticsearch.IElasticsearchIndex;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@AuthAction(action = FULLTEXTINDEX_MODIFY.class)
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

    @Input({@Param(name = "typeList", type = ApiParamType.JSONARRAY, desc = "类型列表"),
            @Param(name = "handler", isRequired = true, rule = "database,elasticsearch", type = ApiParamType.STRING, desc = "处理器")})
    @Output({@Param(explode = FullTextIndexRebuildAuditVo[].class)})
    @Description(desc = "获取索引重建记录列表")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        FullTextIndexRebuildAuditVo audit = JSON.toJavaObject(paramObj, FullTextIndexRebuildAuditVo.class);
        List<FullTextIndexRebuildAuditVo> rebuildAuditList = fullTextIndexRebuildAuditMapper.searchFullTextIndexRebuildAudit(audit);
        if (Objects.equals(audit.getHandler(), FullTextIndexHandlerType.DATABASE.getValue())) {
            List<FullTextIndexTypeVo> fullTextIndexTypeList = FullTextIndexHandlerFactory.getAllTypeList();
            for (FullTextIndexTypeVo typeVo : fullTextIndexTypeList) {
                Optional<FullTextIndexRebuildAuditVo> op = rebuildAuditList.stream().filter(d -> d.getType().equals(typeVo.getType())).findFirst();
                if (!op.isPresent()) {
                    FullTextIndexRebuildAuditVo auditVo = new FullTextIndexRebuildAuditVo();
                    auditVo.setType(typeVo.getType());
                    auditVo.setTypeName(typeVo.getTypeName());
                    auditVo.setHandler(FullTextIndexHandlerType.DATABASE.getValue());
                    auditVo.setIndexCount(fullTextIndexMapper.getFullTextIndexCountByType(typeVo));
                    rebuildAuditList.add(auditVo);
                } else {
                    op.get().setHandler(FullTextIndexHandlerType.DATABASE.getValue());
                    op.get().setTypeName(typeVo.getTypeName());
                    op.get().setIndexCount(fullTextIndexMapper.getFullTextIndexCountByType(typeVo));
                }
            }
        } else if (Objects.equals(audit.getHandler(), FullTextIndexHandlerType.ELASTICSEARCH.getValue())) {
            List<IElasticsearchIndex> elasticsearchIndexList = ElasticsearchIndexFactory.getAllIndex();
            for (IElasticsearchIndex elasticsearchIndex : elasticsearchIndexList) {
                Optional<FullTextIndexRebuildAuditVo> op = rebuildAuditList.stream().filter(d -> d.getType().equals(elasticsearchIndex.getName())).findFirst();
                if (!op.isPresent()) {
                    FullTextIndexRebuildAuditVo auditVo = new FullTextIndexRebuildAuditVo();
                    auditVo.setType(elasticsearchIndex.getName());
                    auditVo.setTypeName(elasticsearchIndex.getLabel());
                    auditVo.setHandler(FullTextIndexHandlerType.ELASTICSEARCH.getValue());
                    auditVo.setIndexCount(elasticsearchIndex.getDocumentCount());
                    rebuildAuditList.add(auditVo);
                } else {
                    op.get().setHandler(FullTextIndexHandlerType.ELASTICSEARCH.getValue());
                    op.get().setTypeName(elasticsearchIndex.getLabel());
                    op.get().setIndexCount(elasticsearchIndex.getDocumentCount());
                }
            }
        }
        return rebuildAuditList;
    }
}
