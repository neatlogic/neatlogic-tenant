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
import neatlogic.framework.datawarehouse.dto.DatabaseVo;
import neatlogic.framework.datawarehouse.service.DatabaseService;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.URLClassLoader;
import java.sql.Connection;

@Service
@AuthAction(action = DATA_WAREHOUSE_MODIFY.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class TestDataBaseApi extends PrivateApiComponentBase {

    @Resource
    private DatabaseService databaseService;

    @Override
    public String getName() {
        return "nmtad.testdatabaseapi.getname";
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "common.id")
    })
    @Output({
            @Param(explode = DatabaseVo.class, desc = "common.tbodylist")
    })
    @Description(desc = "nmtad.testdatabaseapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long id = paramObj.getLong("id");
        Connection conn = databaseService.getConnectionByDatabaseId(id);
        if (conn != null) {
            ClassLoader classLoader = conn.getClass().getClassLoader();
            conn.close();
            if (classLoader instanceof URLClassLoader) {
                ((URLClassLoader) classLoader).close();
            }
        }
        return null;
    }

    @Override
    public String getToken() {
        return "database/test";
    }
}
