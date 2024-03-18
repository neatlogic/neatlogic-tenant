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

package neatlogic.module.tenant.api.integration.table;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.exception.integration.IntegrationHandlerNotFoundException;
import neatlogic.framework.exception.integration.IntegrationNotFoundException;
import neatlogic.framework.integration.core.IIntegrationHandler;
import neatlogic.framework.integration.core.IntegrationHandlerFactory;
import neatlogic.framework.integration.crossover.IntegrationCrossoverService;
import neatlogic.framework.integration.dao.mapper.IntegrationMapper;
import neatlogic.framework.integration.dto.IntegrationVo;
import neatlogic.framework.integration.dto.table.ColumnVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @program: neatlogic
 * @description:
 * @create: 2020-03-26 19:06
 **/
@Service
@Deprecated
@OperationType(type = OperationTypeEnum.SEARCH)
public class TableColumnListApi extends PrivateApiComponentBase {

    @Resource
    private IntegrationMapper integrationMapper;

    @Override
    public String getToken() {
        return "integration/table/column/list";
    }

    @Override
    public String getName() {
        return "集成表格属性列表接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "integrationUuid", type = ApiParamType.STRING, isRequired = true, desc = "集成uuid")
    })
    @Output({
            @Param(name = "tbodyList", explode = ColumnVo[].class, desc = "矩阵属性集合")
    })
    @Description(desc = "矩阵属性检索接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        String integrationUuid = jsonObj.getString("integrationUuid");
        IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(integrationUuid);
        if (integrationVo == null) {
            throw new IntegrationNotFoundException(integrationUuid);
        }
        IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
        if (handler == null) {
            throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
        }

        IntegrationCrossoverService integrationCrossoverService = CrossoverServiceFactory.getApi(IntegrationCrossoverService.class);
        resultObj.put("tbodyList", integrationCrossoverService.getColumnList(integrationVo));
        return resultObj;
    }
}
