/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.integration;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.exception.util.StartTimeAndEndTimeCanNotFoundException;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationAuditVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.TableResultUtil;
import codedriver.framework.util.TimeUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class IntegrationAuditSearchApi extends PrivateApiComponentBase {

    @Autowired
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
            @Param(name = "userUuid", type = ApiParamType.STRING, desc = "用户uuid"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数量"),
            @Param(name = "startTime", type = ApiParamType.LONG, desc = "开始时间"),
            @Param(name = "endTime", type = ApiParamType.LONG, desc = "结束时间"),
            @Param(name = "timeRange", type = ApiParamType.INTEGER, desc = "时间范围"),
            @Param(name = "timeUnit", type = ApiParamType.ENUM, rule = "year,month,week,day,hour", desc = "时间范围单位"),
            @Param(name = "status", type = ApiParamType.STRING, desc = "状态"),
    })
    @Output({
            @Param(explode = BasePageVo.class), @Param(name = "tbodyList", explode = IntegrationAuditVo[].class)
    })
    @Description(desc = "集成调用审计查询接口")
    @Override
    public Object myDoService(JSONObject jsonObj) {
        IntegrationAuditVo paramAuditVo = JSONObject.toJavaObject(jsonObj, IntegrationAuditVo.class);
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
