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

package neatlogic.module.tenant.api.datawarehouse;

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
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
        return "查询数据仓库数据源";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字", xss = true)})
    @Output({@Param(explode = BasePageVo.class),
            @Param(name = "tbodyList", explode = DataSourceVo[].class)})
    @Description(desc = "查询数据仓库数据源接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        DataSourceVo reportDataSourceVo = JSONObject.toJavaObject(jsonObj, DataSourceVo.class);
        List<DataSourceVo> reportDataSourceList = reportDataSourceMapper.searchDataSource(reportDataSourceVo);
        if (CollectionUtils.isNotEmpty(reportDataSourceList)) {
            reportDataSourceVo.setRowNum(reportDataSourceMapper.searchDataSourceCount(reportDataSourceVo));
        }
        return TableResultUtil.getResult(reportDataSourceList, reportDataSourceVo);
    }

}
