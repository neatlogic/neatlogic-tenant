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

package neatlogic.module.tenant.api.constvalue;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.EnumFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;


@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class UniversalSearchNamedEnumApi extends PrivateApiComponentBase {
    @Override
    public String getToken() {
        return "universal/enum/search";
    }

    @Override
    public String getName() {
        return "搜索具名枚举";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "keyword", type = ApiParamType.STRING, desc = "枚举名称")})
    @Output({@Param(name = "name", type = ApiParamType.STRING, desc = "名称"),
            @Param(name = "className", type = ApiParamType.STRING, desc = "类路径")})
    @Description(desc = "搜索具名枚举接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        return EnumFactory.searchEnumClassByName(jsonObj.getString("keyword"));
    }
}
