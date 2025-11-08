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
            @Param(name = "fileIdList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "common.fileidlist"),
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
