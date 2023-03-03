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

package neatlogic.module.tenant.api.healthcheck;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.dto.healthcheck.DatabaseFragmentVo;
import neatlogic.framework.healthcheck.dao.mapper.DatabaseFragmentMapper;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchDatabaseFragmentApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "/healthcheck/databasefragment/search";
    }

    @Override
    public String getName() {
        return "搜索数据文件碎片";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Resource
    private DatabaseFragmentMapper databaseFragmentMapper;


    @Input({@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字"),
            @Param(name = "schemaType", type = ApiParamType.ENUM, rule = "main,data", desc = "库类型"),
            @Param(name = "sortConfig", type = ApiParamType.JSONOBJECT, desc = "排序"), @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")})
    @Output({@Param(explode = BasePageVo.class), @Param(name = "tbodyList", explode = DatabaseFragmentVo[].class)})
    @Description(desc = "搜索数据文件碎片接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        DatabaseFragmentVo databaseFragmentVo = JSONObject.toJavaObject(paramObj, DatabaseFragmentVo.class);
        JSONObject sortConfig = paramObj.getJSONObject("sortConfig");
        List<String> columnList = new ArrayList<>();
        columnList.add("name");
        columnList.add("dataRows");
        columnList.add("dataSize");
        columnList.add("indexSize");
        columnList.add("dataFree");
        List<String> sortList = new ArrayList<>();
        if (MapUtils.isNotEmpty(sortConfig)) {
            for (String key : sortConfig.keySet()) {
                if (columnList.contains(key)) {
                    sortList.add(key + " " + sortConfig.getString(key));
                }
            }
        }
        if (CollectionUtils.isNotEmpty(sortList)) {
            databaseFragmentVo.setSortList(sortList);
        }
        List<DatabaseFragmentVo> databaseFragmentList = databaseFragmentMapper.searchDatabaseFragment(databaseFragmentVo);
        if (CollectionUtils.isNotEmpty(databaseFragmentList)) {
            int rowNum = databaseFragmentMapper.searchDatabaseFragmentCount(databaseFragmentVo);
            databaseFragmentVo.setDataRows(rowNum);
        }
        return TableResultUtil.getResult(databaseFragmentList, databaseFragmentVo);
    }


}
