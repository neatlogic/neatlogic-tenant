/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.systemnotice;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.systemnotice.dao.mapper.SystemNoticeMapper;
import codedriver.framework.systemnotice.dto.SystemNoticeVo;
import codedriver.framework.util.TimeUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service

@OperationType(type = OperationTypeEnum.UPDATE)
public class SystemNoticeReadStatusUpdateApi extends PrivateApiComponentBase {

    @Autowired
    private SystemNoticeMapper systemNoticeMapper;

    @Override
    public String getToken() {
        return "systemnotice/readstatus/update";
    }

    @Override
    public String getName() {
        return "标记未读的系统公告为已读";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "idList", type = ApiParamType.JSONARRAY, desc = "公告ID列表(可不传，如此则把所有公告标为已读)"),
            @Param(name = "startTime", type = ApiParamType.LONG, desc = "开始时间"),
            @Param(name = "endTime", type = ApiParamType.LONG, desc = "结束时间"),
            @Param(name = "timeRange", type = ApiParamType.INTEGER, desc = "时间跨度"),
            @Param(name = "timeUnit", type = ApiParamType.ENUM, rule = "year,month,week,day,hour", desc = "时间跨度单位"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字", xss=true)
    })
    @Output({})
    @Description(desc = "标记未读的系统公告为已读")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray idList = jsonObj.getJSONArray("idList");
        if(CollectionUtils.isNotEmpty(idList)){
            List<Long> list = idList.stream().map(o -> Long.valueOf(o.toString())).collect(Collectors.toList());
            systemNoticeMapper.updateNoticeUserReadStatusByIdList(list, UserContext.get().getUserUuid(true));
        }else{
            SystemNoticeVo vo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<SystemNoticeVo>() {});
            if(vo.getStartTime() == null && vo.getEndTime() == null){
                Integer timeRange = jsonObj.getInteger("timeRange");
                String timeUnit = jsonObj.getString("timeUnit");
                if(timeRange != null && StringUtils.isNotBlank(timeUnit)){
                    vo.setStartTime(TimeUtil.recentTimeTransfer(timeRange, timeUnit));
                    vo.setEndTime(new Date());
                }
            }
            systemNoticeMapper.updateNotReadNoticeToReadByUserUuid(vo,UserContext.get().getUserUuid(true));
        }
        return null;
    }
}
