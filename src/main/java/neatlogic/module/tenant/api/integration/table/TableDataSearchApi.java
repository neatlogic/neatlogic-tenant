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

package neatlogic.module.tenant.api.integration.table;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.exception.integration.*;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.integration.core.IIntegrationHandler;
import neatlogic.framework.integration.core.IntegrationHandlerFactory;
import neatlogic.framework.integration.crossover.IntegrationCrossoverService;
import neatlogic.framework.integration.dao.mapper.IntegrationMapper;
import neatlogic.framework.integration.dto.IntegrationResultVo;
import neatlogic.framework.integration.dto.IntegrationVo;
import neatlogic.framework.integration.dto.table.ColumnVo;
import neatlogic.framework.integration.dto.table.SourceColumnVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.framework.integration.handler.FrameworkRequestFrom;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class TableDataSearchApi extends PrivateApiComponentBase {

    private final static Logger logger = LoggerFactory.getLogger(TableDataSearchApi.class);

    @Resource
    private IntegrationMapper integrationMapper;

    @Override
    public String getToken() {
        return "integration/table/data/search";
    }

    @Override
    public String getName() {
        return "????????????????????????-table??????";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "integrationUuid", desc = "??????Uuid", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "columnList", desc = "??????????????????????????????????????????????????????", type = ApiParamType.JSONARRAY, isRequired = true),
            @Param(name = "defaultValue", desc = "?????????????????????uuid??????", type = ApiParamType.JSONARRAY),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "???????????????????????????true"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "????????????"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "?????????"),
            @Param(name = "searchColumnList ", desc = "??????????????????", type = ApiParamType.JSONARRAY),
            @Param(name = "sourceColumnList ", desc = "?????????????????????", type = ApiParamType.JSONARRAY),
            @Param(name = "filterList", desc = "????????????????????????", type = ApiParamType.JSONARRAY)
    })
    @Description(desc = "????????????????????????-table??????")
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "??????????????????"),
            @Param(name = "theadList", type = ApiParamType.JSONARRAY, desc = "??????????????????"),
            @Param(name = "searchColumnDetailList", type = ApiParamType.JSONARRAY, desc = "????????????????????????"),
            @Param(explode = BasePageVo.class)
    })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        IntegrationCrossoverService integrationCrossoverService = CrossoverServiceFactory.getApi(IntegrationCrossoverService.class);
        return integrationCrossoverService.searchTableData(jsonObj);
    }

}
