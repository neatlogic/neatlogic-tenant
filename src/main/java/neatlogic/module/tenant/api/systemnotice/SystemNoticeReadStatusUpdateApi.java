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
