/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.healthcheck;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.ADMIN;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.dto.healthcheck.DatabaseFragmentVo;
import codedriver.framework.healthcheck.dao.mapper.DatabaseFragmentMapper;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.TableResultUtil;
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
