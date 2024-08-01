/*
 * Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neatlogic.module.tenant.api.datawarehouse;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.DATA_WAREHOUSE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.datawarehouse.dao.mapper.DataWarehouseDataSourceMapper;
import neatlogic.framework.datawarehouse.dto.DataSourceVo;
import neatlogic.framework.datawarehouse.enums.Status;
import neatlogic.framework.datawarehouse.exceptions.DataSourceIsNotFoundException;
import neatlogic.framework.datawarehouse.service.DataSourceService;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

@Service
@AuthAction(action = DATA_WAREHOUSE_MODIFY.class)
@OperationType(type = OperationTypeEnum.OPERATE)
@Transactional
public class ActiveDataSourceApi extends PrivateApiComponentBase {

    @Resource
    private DataWarehouseDataSourceMapper reportDataSourceMapper;


    @Resource
    private DataSourceService reportDataSourceService;


    @Override
    public String getToken() {
        return "datawarehouse/datasource/active";
    }

    @Override
    public String getName() {
        return "nmtad.activedatasourceapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "common.id", isRequired = true),
            @Param(name = "isActive", type = ApiParamType.INTEGER, desc = "common.isactive", isRequired = true)
    })
    @Description(desc = "nmtad.activedatasourceapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        DataSourceVo dataSourceVo = reportDataSourceMapper.getDataSourceById(id);
        if (dataSourceVo == null) {
            throw new DataSourceIsNotFoundException(id);
        }
        Integer isActive = jsonObj.getInteger("isActive");
        if (Objects.equals(dataSourceVo.getIsActive(), isActive)) {
            return null;
        }
        dataSourceVo.setIsActive(isActive);
        reportDataSourceMapper.updateReportDataSourceIsActive(dataSourceVo);
        if (Objects.equals(dataSourceVo.getStatus(), Status.DOING.getValue())) {
            dataSourceVo.setStatus(Status.ABORTED.getValue());
            dataSourceVo.setDataCount(null);
            reportDataSourceMapper.updateReportDataSourceStatus(dataSourceVo);
        }
        reportDataSourceService.loadOrUnloadReportDataSourceJob(dataSourceVo);
        return null;
    }

}
