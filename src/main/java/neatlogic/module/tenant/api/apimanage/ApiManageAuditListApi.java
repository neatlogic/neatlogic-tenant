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

package neatlogic.module.tenant.api.apimanage;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.exception.type.ApiNotFoundException;
import neatlogic.framework.exception.util.StartTimeAndEndTimeCanNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentFactory;
import neatlogic.framework.restful.dao.mapper.ApiAuditMapper;
import neatlogic.framework.restful.dao.mapper.ApiMapper;
import neatlogic.framework.restful.dto.ApiAuditPathVo;
import neatlogic.framework.restful.dto.ApiAuditVo;
import neatlogic.framework.restful.dto.ApiVo;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.framework.util.TimeUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class ApiManageAuditListApi extends PrivateApiComponentBase {

    @Autowired
    private ApiMapper ApiMapper;

    @Autowired
    private ApiAuditMapper apiAuditMapper;

    @Override
    public String getToken() {
        return "apimanage/audit/list";
    }

    @Override
    public String getName() {
        return "??????????????????????????????";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "token", type = ApiParamType.STRING, isRequired = true, desc = "??????token"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "????????????????????????1"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "?????????????????????10"),
            @Param(name = "startTime", type = ApiParamType.LONG, desc = "????????????"),
            @Param(name = "endTime", type = ApiParamType.LONG, desc = "????????????"),
            @Param(name = "timeRange", type = ApiParamType.INTEGER, desc = "????????????"),
            @Param(name = "timeUnit", type = ApiParamType.ENUM, rule = "year,month,week,day,hour", desc = "??????????????????"),
            @Param(name = "userUuidList", type = ApiParamType.JSONARRAY, desc = "??????uuid??????"),
            @Param(name = "statusList", type = ApiParamType.JSONARRAY, desc = "????????????"),
    })
    @Output({
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "????????????"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "?????????"),
            @Param(name = "pageCount", type = ApiParamType.INTEGER, desc = "?????????"),
            @Param(name = "rowNum", type = ApiParamType.INTEGER, desc = "?????????"),
            @Param(name = "tbodyList", explode = ApiAuditVo[].class, isRequired = true, desc = "????????????????????????")
    })
    @Description(desc = "??????????????????????????????")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<ApiAuditVo> apiAuditList = new ArrayList<>();
        ApiAuditVo apiAuditVo = jsonObj.toJavaObject(ApiAuditVo.class);
        if (PrivateApiComponentFactory.getApiByToken(apiAuditVo.getToken()) == null) {
            ApiVo api = ApiMapper.getApiByToken(apiAuditVo.getToken());
            if (api == null) {
                throw new ApiNotFoundException(apiAuditVo.getToken());
            }
        }
        //????????????????????? ???????????????????????????
        if (apiAuditVo.getStartTime() == null && apiAuditVo.getEndTime() == null) {
            Integer timeRange = jsonObj.getInteger("timeRange");
            String timeUnit = jsonObj.getString("timeUnit");
            if (timeRange != null && StringUtils.isNotBlank(timeUnit)) {
                apiAuditVo.setStartTime(TimeUtil.recentTimeTransfer(timeRange, timeUnit));
                apiAuditVo.setEndTime(new Date());
            }
        }

        if (apiAuditVo.getStartTime() == null || apiAuditVo.getEndTime() == null) {
            throw new StartTimeAndEndTimeCanNotFoundException();
        }

        int rowNum = apiAuditMapper.getApiAuditCount(apiAuditVo);
        if (rowNum > 0) {
            apiAuditVo.setRowNum(rowNum);
            apiAuditVo.setPageCount(PageUtil.getPageCount(rowNum, apiAuditVo.getPageSize()));
            apiAuditList = apiAuditMapper.getApiAuditList(apiAuditVo);
        }
        return TableResultUtil.getResult(apiAuditList, apiAuditVo);
    }

}
