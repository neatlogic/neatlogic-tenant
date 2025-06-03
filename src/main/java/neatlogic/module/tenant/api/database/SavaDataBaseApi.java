/*
 * Copyright (C) 2025  深圳极向量科技有限公司 All Rights Reserved.
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

package neatlogic.module.tenant.api.database;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.DATA_WAREHOUSE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.datawarehouse.dao.mapper.DatabaseMapper;
import neatlogic.framework.datawarehouse.dto.DatabaseVo;
import neatlogic.framework.datawarehouse.exceptions.DatabaseNameRepeatException;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.SnowflakeUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@AuthAction(action = DATA_WAREHOUSE_MODIFY.class)
@OperationType(type = OperationTypeEnum.OPERATE)
@Transactional
public class SavaDataBaseApi extends PrivateApiComponentBase {

    @Resource
    private DatabaseMapper databaseMapper;

    @Override
    public String getName() {
        return "nmtad.savadatabaseapi.getname";
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "common.id"),
            @Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "common.name"),
            @Param(name = "type", type = ApiParamType.STRING, isRequired = true, desc = "common.type"),
            @Param(name = "config", type = ApiParamType.JSONOBJECT, isRequired = true, desc = "common.config"),
    })
    @Output({
            @Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "common.id")
    })
    @Description(desc = "nmtad.savadatabaseapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        DatabaseVo dataBaseVo = paramObj.toJavaObject(DatabaseVo.class);
        Long id = paramObj.getLong("id");
        if (id == null) {
            dataBaseVo.setId(SnowflakeUtil.uniqueLong());
        }
        databaseMapper.insertDataBase(dataBaseVo);
        JSONObject resultObj = new JSONObject();
        resultObj.put("id", dataBaseVo.getId());
        return resultObj;
    }

    @Override
    public String getToken() {
        return "database/save";
    }

    public IValid name() {
        return paramObj -> {
            DatabaseVo dataBaseVo = paramObj.toJavaObject(DatabaseVo.class);
            int count = databaseMapper.checkDatabaseNameIsRepeat(dataBaseVo);
            if (count > 0) {
                return new FieldValidResultVo(new DatabaseNameRepeatException(dataBaseVo.getName()));
            }
            return new FieldValidResultVo();
        };
    }
}
