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

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.PropertyBase;
import co.elastic.clients.elasticsearch.indices.GetMappingResponse;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.FULLTEXTINDEX_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.elasticsearch.ElasticSearchClientNotFoundException;
import neatlogic.framework.exception.elasticsearch.ElasticSearchIndexNotFoundException;
import neatlogic.framework.fulltextindex.dto.fulltextindex.FullTextIndexFieldMetaVo;
import neatlogic.framework.fulltextindex.dto.fulltextindex.FullTextIndexMetadataVo;
import neatlogic.framework.fulltextindex.enums.FullTextIndexHandlerType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.store.elasticsearch.ElasticsearchClientFactory;
import neatlogic.framework.store.elasticsearch.ElasticsearchDocumentFactory;
import neatlogic.framework.store.elasticsearch.IElasticsearchDocument;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AuthAction(action = FULLTEXTINDEX_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
@SuppressWarnings("rawtypes")
public class GetFullTextIndexMetadataApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "fulltextindex/metadata/get";
    }

    @Override
    public String getName() {
        return "获取索引元数据";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "type", desc = "索引类型", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "handler", desc = "处理器", rule = "elasticsearch", type = ApiParamType.STRING, isRequired = true)})
    @Output({@Param(explode = FullTextIndexMetadataVo.class)})
    @Description(desc = "获取索引元数据")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String type = paramObj.getString("type");
        String handler = paramObj.getString("handler");
        if (!Objects.equals(FullTextIndexHandlerType.ELASTICSEARCH.getValue(), handler)) {
            throw new ElasticSearchIndexNotFoundException(type);
        }

        // 只从已注册组件中匹配，避免调用会自动创建索引的 ElasticsearchDocumentFactory.getIndex(type)。
        IElasticsearchDocument elasticsearchIndex = findElasticsearchIndex(type);
        String indexName = elasticsearchIndex.getIndexName(elasticsearchIndex.getName());
        FullTextIndexMetadataVo metadataVo = new FullTextIndexMetadataVo();
        metadataVo.setHandler(FullTextIndexHandlerType.ELASTICSEARCH.getValue());
        metadataVo.setType(elasticsearchIndex.getName());
        metadataVo.setTypeName(elasticsearchIndex.getLabel());
        metadataVo.setIndexName(indexName);

        ElasticsearchClient client = ElasticsearchClientFactory.getClient();
        if (client == null) {
            throw new ElasticSearchClientNotFoundException();
        }
        BooleanResponse existsResponse = client.indices().exists(e -> e.index(indexName));
        metadataVo.setIndexExists(existsResponse.value());
        if (!metadataVo.isIndexExists()) {
            metadataVo.setIndexCount(0);
            return metadataVo;
        }
        metadataVo.setIndexCount(elasticsearchIndex.getDocumentCount());
        metadataVo.setFieldList(getFieldMetaList(client, indexName));
        return metadataVo;
    }

    /**
     * 按类型查找已注册的 Elasticsearch 索引组件。
     */
    private IElasticsearchDocument findElasticsearchIndex(String type) {
        Optional<IElasticsearchDocument> op = ElasticsearchDocumentFactory.getAllIndex().stream()
                .filter(index -> Objects.equals(index.getName(), type))
                .findFirst();
        if (op.isEmpty()) {
            throw new ElasticSearchIndexNotFoundException(type);
        }
        return op.get();
    }

    /**
     * 从当前 ES mapping 中提取字段元数据。
     */
    private List<FullTextIndexFieldMetaVo> getFieldMetaList(ElasticsearchClient client, String indexName) throws Exception {
        GetMappingResponse response = client.indices().getMapping(g -> g.index(indexName));
        Map<String, Property> propertyMap = null;
        if (response.result() != null && !response.result().isEmpty()) {
            if (response.result().containsKey(indexName)) {
                propertyMap = response.result().get(indexName).mappings().properties();
            } else {
                propertyMap = response.result().values().iterator().next().mappings().properties();
            }
        }
        List<FullTextIndexFieldMetaVo> fieldMetaList = new ArrayList<>();
        appendFieldMetaList(fieldMetaList, "", propertyMap);
        fieldMetaList.sort(Comparator.comparing(FullTextIndexFieldMetaVo::getPath));
        return fieldMetaList;
    }

    /**
     * 递归展开 object/nested 子字段，同时保留 text.keyword 这类 multi-field。
     */
    private void appendFieldMetaList(List<FullTextIndexFieldMetaVo> fieldMetaList, String parentPath, Map<String, Property> propertyMap) {
        if (propertyMap == null || propertyMap.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Property> entry : propertyMap.entrySet()) {
            String path = parentPath.isEmpty() ? entry.getKey() : parentPath + "." + entry.getKey();
            Property property = entry.getValue();
            fieldMetaList.add(buildFieldMetaVo(entry.getKey(), path, property));
            if (property != null && (property.isObject() || property.isNested())) {
                appendFieldMetaList(fieldMetaList, path, getChildPropertyMap(property));
            }
            appendFieldMetaList(fieldMetaList, path, getMultiFieldPropertyMap(property));
        }
    }

    /**
     * 将 ES Property 转成页面可直接展示的字段元数据。
     */
    private FullTextIndexFieldMetaVo buildFieldMetaVo(String name, String path, Property property) {
        FullTextIndexFieldMetaVo fieldMetaVo = new FullTextIndexFieldMetaVo();
        fieldMetaVo.setName(name);
        fieldMetaVo.setPath(path);
        if (property == null) {
            return fieldMetaVo;
        }
        fieldMetaVo.setType(property._isCustom() ? property._customKind() : property._kind().jsonValue());
        if (property.isText()) {
            fieldMetaVo.setAnalyzer(property.text().analyzer());
            fieldMetaVo.setSearchAnalyzer(property.text().searchAnalyzer());
        } else if (property.isKeyword()) {
            fieldMetaVo.setNormalizer(property.keyword().normalizer());
        } else if (property.isDate()) {
            fieldMetaVo.setFormat(property.date().format());
        } else if (property.isDateNanos()) {
            fieldMetaVo.setFormat(property.dateNanos().format());
        }
        return fieldMetaVo;
    }

    /**
     * 获取 object/nested 的子字段定义。
     */
    private Map<String, Property> getChildPropertyMap(Property property) {
        if (property == null || !(property._get() instanceof PropertyBase)) {
            return null;
        }
        return ((PropertyBase) property._get()).properties();
    }

    /**
     * 获取 multi-field 定义，例如 text 字段下的 keyword 子字段。
     */
    private Map<String, Property> getMultiFieldPropertyMap(Property property) {
        if (property == null || !(property._get() instanceof PropertyBase)) {
            return null;
        }
        return ((PropertyBase) property._get()).fields();
    }
}
