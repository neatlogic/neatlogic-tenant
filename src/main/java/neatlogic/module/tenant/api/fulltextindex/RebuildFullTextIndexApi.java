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

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.FULLTEXTINDEX_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.elasticsearch.ElasticSearchIndexNotFoundException;
import neatlogic.framework.exception.fulltextindex.FullTextIndexHandlerNotFoundException;
import neatlogic.framework.exception.qdrant.QdrantCollectionNotFoundException;
import neatlogic.framework.fulltextindex.core.FullTextIndexHandlerFactory;
import neatlogic.framework.fulltextindex.core.IFullTextIndexHandler;
import neatlogic.framework.fulltextindex.enums.FullTextIndexHandlerType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.store.elasticsearch.ElasticsearchDocumentFactory;
import neatlogic.framework.store.elasticsearch.IElasticsearchDocument;
import neatlogic.framework.store.qdrant.IQdrantCollection;
import neatlogic.framework.store.qdrant.QdrantCollectionFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@AuthAction(action = FULLTEXTINDEX_MODIFY.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class RebuildFullTextIndexApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "fulltextindex/rebuild";
    }

    @Override
    public String getName() {
        return "重建检索索引";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "type", desc = "索引类型", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "handler", desc = "处理器", rule = "database,elasticsearch,qdrant", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "isAll", desc = "是否全部重建", type = ApiParamType.BOOLEAN, isRequired = true)})
    @Description(desc = "重建检索索引")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String type = paramObj.getString("type");
        boolean isAll = paramObj.getBooleanValue("isAll");
        String handler = paramObj.getString("handler");
        if (Objects.equals(FullTextIndexHandlerType.DATABASE.getValue(), handler)) {
            IFullTextIndexHandler fulltextHandler = FullTextIndexHandlerFactory.getHandler(type);
            if (fulltextHandler == null) {
                throw new FullTextIndexHandlerNotFoundException(type);
            }
            fulltextHandler.rebuildIndex(type, isAll);
        } else if (Objects.equals(FullTextIndexHandlerType.ELASTICSEARCH.getValue(), handler)) {
            IElasticsearchDocument fulltextHandler = ElasticsearchDocumentFactory.getIndex(type);
            if (fulltextHandler == null) {
                throw new ElasticSearchIndexNotFoundException(type);
            }
            fulltextHandler.rebuildDocument(isAll);
        }
        else if (Objects.equals(FullTextIndexHandlerType.QDRANT.getValue(), handler)) {
            IQdrantCollection collection = QdrantCollectionFactory.getCollection(type);
            if (collection == null) {
                throw new QdrantCollectionNotFoundException(type);
            }
            collection.rebuildPoint(isAll);
        }
        return null;
    }
}
