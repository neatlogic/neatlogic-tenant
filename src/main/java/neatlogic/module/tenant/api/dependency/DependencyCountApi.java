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

package neatlogic.module.tenant.api.dependency;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dependency.core.DependencyManager;
import neatlogic.framework.dependency.core.FromTypeFactory;
import neatlogic.framework.dependency.core.IFromType;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class DependencyCountApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "dependency/count";
    }

    @Override
    public String getName() {
        return "查询引用数量";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "defaultValue", type = ApiParamType.JSONARRAY, isRequired = true, minSize = 1, desc = "被调用者唯一标识是字符串类型的时候，通过uuid参数传入", help = "被调用者唯一标识是数字类型的时候，通过id参数传入；被调用者唯一标识是数字类型的时候，通过id参数传入"),
            @Param(name = "calleeType", type = ApiParamType.STRING, isRequired = true, desc = "被调用者类型")
    })
    @Output({
            @Param(name = "Return", type = ApiParamType.JSONOBJECT, desc = "引用数量")
    })
    @Description(desc = "查询引用数量")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        IFromType calleeType = FromTypeFactory.getCalleeType(paramObj.getString("calleeType"));
        if (calleeType == null) {
            throw new ParamIrregularException("calleeType（被调用者类型）", FromTypeFactory.getAllCalleeTypeToString());
        }
        JSONArray defaultValue = paramObj.getJSONArray("defaultValue");
        return DependencyManager.getBatchDependencyCount(calleeType, defaultValue);
    }
}
