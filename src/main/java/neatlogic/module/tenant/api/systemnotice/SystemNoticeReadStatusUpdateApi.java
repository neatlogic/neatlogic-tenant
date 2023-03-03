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

package neatlogic.module.tenant.api.systemnotice;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.systemnotice.dao.mapper.SystemNoticeMapper;
import neatlogic.framework.systemnotice.dto.SystemNoticeVo;
import neatlogic.framework.util.TimeUtil;
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
