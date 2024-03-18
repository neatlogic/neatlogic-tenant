/*Copyright (C) 2023  深圳极向量科技有限公司 All Rights Reserved.

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

package neatlogic.module.tenant.api.mongodb;

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.dto.MongoDbVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.publicapi.PublicApiComponentBase;
import neatlogic.framework.restful.dao.mapper.NeatLogicMapper;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class MongoDbDataSourceListPublicApi extends PublicApiComponentBase {
    @Autowired
    NeatLogicMapper neatlogicMapper;

    @Override
    public String getToken() {
        return "/public/mongodb/datasource/list";
    }

    @Override
    public String getName() {
        return "获取所有mongodb连接信息";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
    })
    @Output({
            @Param(name = "mongoVo", explode = MongoDbVo[].class, desc = "mongodb数据库")
    })
    @Description(desc = "获取所有mongodb连接信息接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String tenant = TenantContext.get().getTenantUuid();
        TenantContext.get().setUseDefaultDatasource(true);
        MongoDbVo mongodbVo = neatlogicMapper.getMongodbList();
        TenantContext.get().switchTenant(tenant);
        TenantContext.get().setUseDefaultDatasource(false);
        return mongodbVo;
    }
}
