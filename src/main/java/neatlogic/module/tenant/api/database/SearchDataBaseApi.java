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
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.datawarehouse.dao.mapper.DatabaseMapper;
import neatlogic.framework.datawarehouse.dto.DatabaseVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = DATA_WAREHOUSE_MODIFY.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class SearchDataBaseApi extends PrivateApiComponentBase {

    @Resource
    private DatabaseMapper databaseMapper;

    @Override
    public String getName() {
        return "nmtad.searchdatabaseapi.getname";
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword"),
            @Param(name = "type", type = ApiParamType.STRING, desc = "common.type"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize"),
    })
    @Output({
            @Param(name = "tbodyList", explode = DatabaseVo[].class, desc = "common.tbodylist"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "nmtad.searchdatabaseapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        DatabaseVo searchVo = paramObj.toJavaObject(DatabaseVo.class);
        List<DatabaseVo> tbodyList = new ArrayList<>();
        int rowNum = databaseMapper.getDataBaseCount(searchVo);
        if (rowNum > 0) {
            searchVo.setRowNum(rowNum);
            tbodyList = databaseMapper.getDataBaseList(searchVo);
        }
        return TableResultUtil.getResult(tbodyList, searchVo);
    }

    @Override
    public String getToken() {
        return "database/search";
    }
}
