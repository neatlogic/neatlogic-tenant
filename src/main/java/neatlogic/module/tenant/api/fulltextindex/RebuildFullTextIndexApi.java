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
import neatlogic.framework.exception.fulltextindex.FullTextIndexHandlerNotFoundException;
import neatlogic.framework.fulltextindex.core.FullTextIndexHandlerFactory;
import neatlogic.framework.fulltextindex.core.IFullTextIndexHandler;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

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
