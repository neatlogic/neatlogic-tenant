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

package neatlogic.module.tenant.api.message;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.message.dao.mapper.MessageMapper;
import neatlogic.framework.message.dto.MessageSearchVo;
import neatlogic.framework.notify.core.NotifyPolicyHandlerFactory;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TimeUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service

@OperationType(type = OperationTypeEnum.UPDATE)
@Transactional
public class MessageIsReadUpdateApi extends PrivateApiComponentBase {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public String getToken() {
        return "message/isread/update";
    }

    @Override
    public String getName() {
        return "更新消息为已读";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "messageId", type = ApiParamType.LONG, desc = "消息id"),
            @Param(name = "messageIdList", type = ApiParamType.JSONARRAY, desc = "消息id列表"),
            @Param(name = "daysAgo", type = ApiParamType.INTEGER, desc = "距离今天的天数"),
            @Param(name = "keyword", type = ApiParamType.STRING, xss = true, desc = "消息标题，模糊搜索"),
            @Param(name = "messageTypePath", type = ApiParamType.JSONARRAY, desc = "消息分类路径"),
            @Param(name = "startTime", type = ApiParamType.LONG, desc = "开始时间"),
            @Param(name = "endTime", type = ApiParamType.LONG, desc = "结束时间"),
            @Param(name = "timeRange", type = ApiParamType.INTEGER, desc = "时间范围"),
            @Param(name = "timeUnit", type = ApiParamType.ENUM, rule = "year,month,week,day,hour", desc = "时间范围单位")
    })
    @Output({
            @Param(name = "unreadCount", type = ApiParamType.INTEGER, desc = "未读消息数量")
    })
    @Description(desc = "更新消息为已读")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String userUuid = UserContext.get().getUserUuid(true);
        MessageSearchVo searchVo = JSONObject.toJavaObject(jsonObj, MessageSearchVo.class);
        searchVo.setUserUuid(userUuid);
        Long messageId = jsonObj.getLong("messageId");
        if (searchVo.getMessageId() != null) {
            messageMapper.updateMessageUserIsReadByUserUuidAndMessageId(searchVo);
        } else {
            if (CollectionUtils.isNotEmpty(searchVo.getMessageIdList())) {
                messageMapper.updateMessageUserIsReadByUserUuidAndMessageIdList(searchVo);
            } else {
                Integer daysAgo = jsonObj.getInteger("daysAgo");
                if (daysAgo != null) {
                    Date currentDay = Date.from(LocalDate.now().minusDays(daysAgo).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
                    Long fromMessageId = messageMapper.getMessageMaxIdByLessThanInsertTime(currentDay);
                    Date nextDay = new Date(currentDay.getTime() + TimeUnit.HOURS.toMillis(24));
                    Long toMessageId = messageMapper.getMessageMaxIdByLessThanInsertTime(nextDay);
                    searchVo.setMinMessageId(fromMessageId);
                    searchVo.setMaxMessageId(toMessageId);
                    messageMapper.updateMessageUserIsReadByUserUuidAndMessageIdRange(searchVo);
                } else {
                    if (searchVo.getStartTime() == null && searchVo.getEndTime() == null) {
                        Integer timeRange = jsonObj.getInteger("timeRange");
                        String timeUnit = jsonObj.getString("timeUnit");
                        if (timeRange != null && StringUtils.isNotBlank(timeUnit)) {
                            searchVo.setStartTime(TimeUtil.recentTimeTransfer(timeRange, timeUnit));
                            searchVo.setEndTime(new Date());
                        }
                    }
                    if (searchVo.getStartTime() != null || searchVo.getEndTime() != null) {
                        JSONArray messageTypePath = jsonObj.getJSONArray("messageTypePath");
                        if(CollectionUtils.isNotEmpty(messageTypePath)){
                            if(messageTypePath.size() == 1){
                                searchVo.setTriggerList(NotifyPolicyHandlerFactory.getTriggerList(messageTypePath.getString(0)));
                            }else if(messageTypePath.size() == 2){
                                searchVo.setNotifyPolicyHandler(messageTypePath.getString(1));
                            }else if(messageTypePath.size() == 3){
                                searchVo.setNotifyPolicyHandler(messageTypePath.getString(1));
                                List<String> triggerList = new ArrayList<>();
                                triggerList.add(messageTypePath.getString(2));
                                searchVo.setTriggerList(triggerList);
                            }
                        }
                        messageMapper.updateMessageUserIsReadByUserUuidAndKeywordAndTriggerList(searchVo);
                    } else {
                        messageMapper.updateMessageUserIsReadByUserUuid(userUuid);
                    }
                }
            }
        }
        JSONObject resultObj = new JSONObject();
        searchVo = new MessageSearchVo();
        searchVo.setUserUuid(userUuid);
        int unreadCount = messageMapper.getMessageCount(searchVo);
        resultObj.put("unreadCount", unreadCount);
        return resultObj;
    }
}
