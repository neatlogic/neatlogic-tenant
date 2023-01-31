/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
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
        return "接口调用记录列表接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "token", type = ApiParamType.STRING, isRequired = true, desc = "接口token"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页码，默认值1"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "页大小，默认值10"),
            @Param(name = "startTime", type = ApiParamType.LONG, desc = "开始时间"),
            @Param(name = "endTime", type = ApiParamType.LONG, desc = "结束时间"),
            @Param(name = "timeRange", type = ApiParamType.INTEGER, desc = "时间范围"),
            @Param(name = "timeUnit", type = ApiParamType.ENUM, rule = "year,month,week,day,hour", desc = "时间范围单位"),
            @Param(name = "userUuidList", type = ApiParamType.JSONARRAY, desc = "用户uuid列表"),
            @Param(name = "statusList", type = ApiParamType.JSONARRAY, desc = "状态列表"),
    })
    @Output({
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页码"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "页大小"),
            @Param(name = "pageCount", type = ApiParamType.INTEGER, desc = "总页数"),
            @Param(name = "rowNum", type = ApiParamType.INTEGER, desc = "总行数"),
            @Param(name = "tbodyList", explode = ApiAuditVo[].class, isRequired = true, desc = "接口调用记录列表")
    })
    @Description(desc = "接口调用记录列表接口")
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
        //将时间范围转为 开始时间、结束时间
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
