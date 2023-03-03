/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.fulltextindex;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.fulltextindex.core.FullTextIndexHandlerFactory;
import neatlogic.framework.fulltextindex.core.IFullTextIndexHandler;
import neatlogic.framework.fulltextindex.dao.mapper.FullTextIndexRebuildAuditMapper;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.exception.fulltextindex.FullTextIndexHandlerNotFoundException;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@OperationType(type = OperationTypeEnum.OPERATE)
public class RebuildFullTextIndexApi extends PrivateApiComponentBase {
    @Resource
    private FullTextIndexRebuildAuditMapper fullTextIndexRebuildAuditMapper;

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
            @Param(name = "isAll", desc = "是否全部重建", type = ApiParamType.BOOLEAN, isRequired = true)})
    @Description(desc = "重建检索索引接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String type = paramObj.getString("type");
        boolean isAll = paramObj.getBooleanValue("isAll");
        IFullTextIndexHandler handler = FullTextIndexHandlerFactory.getHandler(type);
        if (handler == null) {
            throw new FullTextIndexHandlerNotFoundException(type);
        }
        handler.rebuildIndex(type, isAll);
        return null;
    }
}
