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
import neatlogic.framework.datawarehouse.dao.mapper.DataWarehouseDataSourceMapper;
import neatlogic.framework.datawarehouse.dto.DataSourceVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = DATA_WAREHOUSE_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetDataSourceStatusApi extends PrivateApiComponentBase {

    @Resource
    private DataWarehouseDataSourceMapper reportDataSourceMapper;

    @Override
    public String getToken() {
        return "datawarehouse/datasource/status/get";
    }

    @Override
    public String getName() {
        return "获取数据仓库数据源状态";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "idList", type = ApiParamType.JSONARRAY, desc = "idl列表", isRequired = true)})
    @Output({@Param(explode = DataSourceVo[].class)})
    @Description(desc = "获取数据仓库数据源状态接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<Long> idList = new ArrayList<>();
        for (int i = 0; i < jsonObj.getJSONArray("idList").size(); i++) {
            idList.add(jsonObj.getJSONArray("idList").getLong(i));
        }
        if (CollectionUtils.isNotEmpty(idList)) {
            return reportDataSourceMapper.getDataSourceByIdList(idList);
        }
        return null;
    }

}
