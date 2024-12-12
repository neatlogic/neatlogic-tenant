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

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.elasticsearch.ElasticSearchIndexNotFoundException;
import neatlogic.framework.exception.fulltextindex.FullTextIndexHandlerNotFoundException;
import neatlogic.framework.fulltextindex.core.FullTextIndexHandlerFactory;
import neatlogic.framework.fulltextindex.core.IFullTextIndexHandler;
import neatlogic.framework.fulltextindex.enums.FullTextIndexHandlerType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.store.elasticsearch.ElasticsearchIndexFactory;
import neatlogic.framework.store.elasticsearch.IElasticsearchIndex;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
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
            @Param(name = "handler", desc = "处理器", rule = "database,elasticsearch", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "isAll", desc = "是否全部重建", type = ApiParamType.BOOLEAN, isRequired = true)})
    @Description(desc = "重建检索索引接口")
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
        } else if (Objects.equals(FullTextIndexHandlerType.ELASTICSEARCH.getValue(), handler)){
            IElasticsearchIndex fulltextHandler = ElasticsearchIndexFactory.getIndex(type);
            if (fulltextHandler == null) {
                throw new ElasticSearchIndexNotFoundException(type);
            }
            fulltextHandler.rebuildDocument(isAll);
        }
        return null;
    }
}
