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

package neatlogic.module.tenant.api.integration;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.exception.util.StartTimeAndEndTimeCanNotFoundException;
import neatlogic.framework.integration.dao.mapper.IntegrationMapper;
import neatlogic.framework.integration.dto.IntegrationAuditVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.framework.util.TimeUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class IntegrationAuditSearchApi extends PrivateApiComponentBase {

    @Resource
    private IntegrationMapper integrationMapper;

    @Override
    public String getToken() {
        return "integration/audit/search";
    }

    @Override
    public String getName() {
        return "查询集成调用审计";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "integrationUuid", type = ApiParamType.STRING, desc = "集成设置uuid"),
            @Param(name = "integrationUuidList", type = ApiParamType.JSONARRAY, desc = "集成设置uuid列表"),
            @Param(name = "userUuidList", type = ApiParamType.JSONARRAY, desc = "用户uuid"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数量"),
            @Param(name = "startTime", type = ApiParamType.LONG, desc = "开始时间"),
            @Param(name = "endTime", type = ApiParamType.LONG, desc = "结束时间"),
            @Param(name = "timeRange", type = ApiParamType.INTEGER, desc = "时间范围"),
            @Param(name = "timeUnit", type = ApiParamType.ENUM, rule = "year,month,week,day,hour", desc = "时间范围单位"),
            @Param(name = "statusList", type = ApiParamType.JSONARRAY, desc = "状态"),
    })
    @Output({
            @Param(explode = BasePageVo.class), @Param(name = "tbodyList", explode = IntegrationAuditVo[].class)
    })
    @Description(desc = "查询集成调用审计")
    @Override
    public Object myDoService(JSONObject jsonObj) {
        IntegrationAuditVo paramAuditVo = JSON.toJavaObject(jsonObj, IntegrationAuditVo.class);
        List<IntegrationAuditVo> returnList = null;

        //将时间范围转为 开始时间、结束时间
        if (paramAuditVo.getStartTime() == null && paramAuditVo.getEndTime() == null) {
            Integer timeRange = jsonObj.getInteger("timeRange");
            String timeUnit = jsonObj.getString("timeUnit");
            if (timeRange != null && StringUtils.isNotBlank(timeUnit)) {
                paramAuditVo.setStartTime(TimeUtil.recentTimeTransfer(timeRange, timeUnit));
                paramAuditVo.setEndTime(new Date());
            }
        }

        if (paramAuditVo.getStartTime() == null || paramAuditVo.getEndTime() == null) {
            throw new StartTimeAndEndTimeCanNotFoundException();
        }

        int auditCount = integrationMapper.getIntegrationAuditCount(paramAuditVo);
        if (auditCount > 0) {
            paramAuditVo.setRowNum(auditCount);
            returnList = integrationMapper.searchIntegrationAudit(paramAuditVo);
        }

        if (CollectionUtils.isEmpty(returnList)) {
            returnList = new ArrayList<>();
        }

        return TableResultUtil.getResult(returnList, paramAuditVo);
    }
}
