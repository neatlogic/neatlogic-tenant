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
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dao.plugin.SqlCostInterceptor;
import neatlogic.framework.dto.healthcheck.SqlAuditVo;
import neatlogic.framework.healthcheck.SqlAuditManager;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class SearchSqlAuditApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "/healthcheck/sqldump";
    }

    @Override
    public String getName() {
        return "获取SQL耗时";
    }

    @Override
    public String getConfig() {
        return null;
    }


    @Input({@Param(name = "id", type = ApiParamType.STRING, desc = "sql语句id"),
            @Param(name = "orderBy", type = ApiParamType.ENUM, rule = "timecost,runtime", desc = "排序，只支持timecost和rumtime"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
    })
    @Output({
            @Param(name = "tbodyList", explode = SqlAuditVo[].class),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "获取SQL耗时接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        //复制一个新列表，不改变原来的列表
        List<SqlAuditVo> sqlAuditList = new ArrayList<>(SqlAuditManager.getSqlAuditList());
        String orderBy = StringUtils.isNotBlank(paramObj.getString("orderBy")) ? paramObj.getString("orderBy") : "runtime";
        String id = paramObj.getString("id");
        int pageSize = 20;
        int currentPage = paramObj.getIntValue("currentPage");
        if (StringUtils.isNotBlank(id)) {
            sqlAuditList = sqlAuditList.stream().filter(d -> d.getId().toLowerCase(Locale.ROOT).contains(id.toLowerCase(Locale.ROOT))).collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(orderBy)) {
            if (orderBy.equals("timecost")) {
                sqlAuditList = sqlAuditList.stream().sorted((o1, o2) -> o2.getTimeCost().compareTo(o1.getTimeCost())).collect(Collectors.toList());
            } else {
                sqlAuditList = sqlAuditList.stream().sorted((o1, o2) -> o2.getRunTime().compareTo(o1.getRunTime())).collect(Collectors.toList());
            }
        }
        long maxTimeCost = 0;
        for (SqlAuditVo sqlAuditVo : sqlAuditList) {
            if (maxTimeCost < sqlAuditVo.getTimeCost()) {
                maxTimeCost = sqlAuditVo.getTimeCost();
            }
        }
        int rowNum = sqlAuditList.size();
        int pageCount = PageUtil.getPageCount(rowNum, pageSize);
        currentPage = Math.max(1, currentPage);
        currentPage = Math.min(currentPage, pageCount);
        try {
            //因为数据可能在访问途中被清空掉，所以如果有异常直接返回空的列表
            sqlAuditList = sqlAuditList.stream().skip((long) (currentPage - 1) * pageSize).limit(pageSize).collect(Collectors.toList());
        } catch (Exception ex) {
            sqlAuditList = new ArrayList<>();
        }
        JSONObject returnObj = new JSONObject();
        returnObj.put("pageCount", pageCount);
        returnObj.put("currentPage", currentPage);
        returnObj.put("pageSize", pageSize);
        returnObj.put("rowNum", rowNum);
        returnObj.put("tbodyList", sqlAuditList);
        returnObj.put("maxTimeCost", maxTimeCost);//用来给前端计算进度条
        returnObj.put("sqlIdList", SqlCostInterceptor.SqlIdMap.getSqlIdList());
        return returnObj;
    }


   /* public static void main(String[] a) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(Integer.toString(i));
        }
        list = list.stream().skip(1).limit(1).collect(Collectors.toList());
        System.out.println(String.join(",", list));
    }*/
}
