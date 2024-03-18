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

package neatlogic.module.tenant.api.globalsearch.documenttype;

import neatlogic.framework.fulltextindex.core.FullTextIndexHandlerFactory;
import neatlogic.framework.fulltextindex.dto.fulltextindex.FullTextIndexTypeVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;

import java.util.stream.Collectors;

//@Service
//@OperationType(type = OperationTypeEnum.SEARCH)
public class ListDocumentTypeApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "globalsearch/document/type/list";
    }

    @Override
    public String getName() {
        return "获取全局搜索文档类型";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({@Param(explode = FullTextIndexTypeVo[].class)})
    @Description(desc = "获取全局搜索文档类型接口")
    @Override
    public Object myDoService(JSONObject jsonObj) {
        return FullTextIndexHandlerFactory.getAllTypeList().stream().filter(FullTextIndexTypeVo::isActiveGlobalSearch).collect(Collectors.toList());
    }

}
