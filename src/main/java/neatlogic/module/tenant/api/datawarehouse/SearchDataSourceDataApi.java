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

package neatlogic.module.tenant.api.datawarehouse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.DATA_WAREHOUSE_BASE;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.datawarehouse.dto.DataSourceDataVo;
import neatlogic.framework.datawarehouse.service.DataSourceService;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.$;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = DATA_WAREHOUSE_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchDataSourceDataApi extends PrivateApiComponentBase {

    @Resource
    private DataSourceService dataSourceService;


    @Override
    public String getToken() {
        return "datawarehouse/datasource/data/search";//
    }

    @Override
    public String getName() {
        return $.t("nmtad.searchdatasourcedataapi.getname");
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "dataSourceId", type = ApiParamType.LONG, desc = "nmtad.searchdatasourcedataapi.input.param.desc.datasourceid", isRequired = true),
            @Param(name = "conditionList", type = ApiParamType.JSONARRAY, desc = "nmtad.searchdatasourcedataapi.input.param.desc.conditionlist"),
            @Param(name = "sortList", type = ApiParamType.JSONARRAY, desc = "nmtad.searchdatasourcedataapi.input.param.desc.sortlist"),
            @Param(name = "limit", type = ApiParamType.INTEGER, desc = "nmtad.searchdatasourcedataapi.input.param.desc.limit"),
            @Param(name = "isExpired", type = ApiParamType.INTEGER, desc = "nmtad.searchdatasourcedataapi.input.param.desc.isexpired")})
    @Output({@Param(explode = BasePageVo.class)})
    @Description(desc = "nmtad.searchdatasourcedataapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        DataSourceDataVo reportDataSourceDataVo = JSON.toJavaObject(jsonObj, DataSourceDataVo.class);
        // 查询委托公共数据源服务，条件、排序、限行与表头组装沿用原有规则。
        return dataSourceService.searchDataSourceData(reportDataSourceDataVo);
    }

}
