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
import neatlogic.framework.datawarehouse.dao.mapper.DataWarehouseDataSourceMapper;
import neatlogic.framework.datawarehouse.dto.DataSourceVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = DATA_WAREHOUSE_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchDataSourceApi extends PrivateApiComponentBase {

    @Resource
    private DataWarehouseDataSourceMapper reportDataSourceMapper;

    @Override
    public String getToken() {
        return "datawarehouse/datasource/search";
    }

    @Override
    public String getName() {
        return "nmtad.searchdatasourceapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "moduleId", type = ApiParamType.STRING, desc = "nfdd.datasourcevo.entityfield.name.moduleid"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword", xss = true)})
    @Output({@Param(explode = BasePageVo.class),
            @Param(name = "tbodyList", explode = DataSourceVo[].class)})
    @Description(desc = "nmtad.searchdatasourceapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        DataSourceVo reportDataSourceVo = JSON.toJavaObject(jsonObj, DataSourceVo.class);
        if (CollectionUtils.isNotEmpty(reportDataSourceVo.getDefaultValue())) {
            List<Long> idList = new ArrayList<>();
            for (int i = 0; i < reportDataSourceVo.getDefaultValue().size(); i++) {
                idList.add(reportDataSourceVo.getDefaultValue().getLongValue(i));
            }
            reportDataSourceVo.setIdList(idList);
        }
        List<DataSourceVo> reportDataSourceList = reportDataSourceMapper.searchDataSource(reportDataSourceVo);
        if (CollectionUtils.isNotEmpty(reportDataSourceList)) {
            reportDataSourceVo.setRowNum(reportDataSourceMapper.searchDataSourceCount(reportDataSourceVo));
        }
        return TableResultUtil.getResult(reportDataSourceList, reportDataSourceVo);
    }

}
