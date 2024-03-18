/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

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
