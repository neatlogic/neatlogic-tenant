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

package neatlogic.module.tenant.api.apiaudit;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.restful.dto.ApiAuditVo;
import neatlogic.module.tenant.service.apiaudit.ApiAuditService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 操作审计查询接口
 */

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class ApiAuditSearchApi extends PrivateApiComponentBase {

    @Autowired
    private ApiAuditService apiAuditService;

    @Override
    public String getToken() {
        return "apiaudit/search";
    }

    @Override
    public String getName() {
        return "查询操作审计";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "moduleGroup", type = ApiParamType.STRING, desc = "API所属模块"),
            @Param(name = "funcId", type = ApiParamType.STRING, desc = "API所属功能"),
            @Param(name = "userUuid", type = ApiParamType.STRING, desc = "访问者UUID"),
            @Param(name = "operationType", type = ApiParamType.STRING, desc = "操作类型"),
            @Param(name = "timeRange", type = ApiParamType.INTEGER, desc = "时间跨度"),
            @Param(name = "timeUnit", type = ApiParamType.STRING, desc = "时间跨度单位(day|month)"),
            @Param(name = "orderType", type = ApiParamType.STRING, desc = "排序类型(asc|desc)"),
            @Param(name = "startTime", type = ApiParamType.LONG, desc = "开始时间"),
            @Param(name = "endTime", type = ApiParamType.LONG, desc = "结束时间"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "搜索关键词")
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "tbodyList", desc = "数据列表", type = ApiParamType.JSONARRAY)
    })
    @Description(desc = "查询操作审计")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ApiAuditVo apiAuditVo = JSON.toJavaObject(jsonObj, ApiAuditVo.class);
        //筛选出符合条件的所有记录
        List<ApiAuditVo> apiAuditVoList = apiAuditService.searchApiAuditVo(apiAuditVo);
        JSONObject returnObj = new JSONObject();
        if (apiAuditVo.getNeedPage()) {
            apiAuditVo.setPageCount(PageUtil.getPageCount(apiAuditVo.getRowNum(), apiAuditVo.getPageSize()));
            returnObj.put("pageSize", apiAuditVo.getPageSize());
            returnObj.put("currentPage", apiAuditVo.getCurrentPage());
            returnObj.put("rowNum", apiAuditVo.getRowNum());
            returnObj.put("pageCount", apiAuditVo.getPageCount());
        }
        returnObj.put("tbodyList", apiAuditVoList);

        return returnObj;
    }
}
