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
import neatlogic.framework.store.qdrant.IQdrantCollection;
import neatlogic.framework.store.qdrant.QdrantCollectionFactory;
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
            @Param(name = "handler", isRequired = true, rule = "database,elasticsearch,qdrant", type = ApiParamType.STRING, desc = "处理器")})
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
                if (op.isEmpty()) {
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
            rebuildAuditList.removeIf(d -> fullTextIndexTypeList.stream().noneMatch(dd -> dd.getType().equalsIgnoreCase(d.getType())));
        } else if (Objects.equals(audit.getHandler(), FullTextIndexHandlerType.ELASTICSEARCH.getValue())) {
            List<IElasticsearchIndex> elasticsearchIndexList = ElasticsearchIndexFactory.getAllIndex();
            for (IElasticsearchIndex elasticsearchIndex : elasticsearchIndexList) {
                Optional<FullTextIndexRebuildAuditVo> op = rebuildAuditList.stream().filter(d -> d.getType().equals(elasticsearchIndex.getName())).findFirst();
                if (op.isEmpty()) {
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
            rebuildAuditList.removeIf(d -> elasticsearchIndexList.stream().noneMatch(dd -> dd.getName().equalsIgnoreCase(d.getType())));
        } else if (Objects.equals(audit.getHandler(), FullTextIndexHandlerType.QDRANT.getValue())) {
            List<IQdrantCollection> qdrantCollectionList = QdrantCollectionFactory.getAllCollection();
            for (IQdrantCollection collection : qdrantCollectionList) {
                Optional<FullTextIndexRebuildAuditVo> op = rebuildAuditList.stream().filter(d -> d.getType().equals(collection.getName())).findFirst();
                if (op.isEmpty()) {
                    FullTextIndexRebuildAuditVo auditVo = new FullTextIndexRebuildAuditVo();
                    auditVo.setType(collection.getName());
                    auditVo.setTypeName(collection.getLabel());
                    auditVo.setHandler(FullTextIndexHandlerType.QDRANT.getValue());
                    auditVo.setIndexCount(collection.getPointCount());
                    rebuildAuditList.add(auditVo);
                } else {
                    op.get().setHandler(FullTextIndexHandlerType.QDRANT.getValue());
                    op.get().setTypeName(collection.getLabel());
                    op.get().setIndexCount(collection.getPointCount());
                }
            }
            rebuildAuditList.removeIf(d -> qdrantCollectionList.stream().noneMatch(dd -> dd.getName().equalsIgnoreCase(d.getType())));
        }
        return rebuildAuditList;
    }
}
