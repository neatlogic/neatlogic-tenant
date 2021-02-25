package codedriver.module.tenant.api.message;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.message.dao.mapper.MessageMapper;
import codedriver.framework.message.dto.MessageSearchVo;
import codedriver.framework.message.dto.MessageVo;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Title: MessageIsReadUpdateApi
 * @Package codedriver.module.tenant.api.message
 * @Description: 更新消息为已删除接口
 * @Author: linbq
 * @Date: 2021/1/4 23:12
 * Copyright(c) 2020 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
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
            @Param(name = "messageType", type = ApiParamType.STRING, desc = "消息分类")
    })
    @Output({
            @Param(name = "newCount", type = ApiParamType.INTEGER, desc = "新消息总数")
    })
    @Description(desc = "更新消息为已读")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String userUuid = UserContext.get().getUserUuid(true);
        MessageSearchVo searchVo = new MessageSearchVo();
        searchVo.setUserUuid(userUuid);
        Long messageId = jsonObj.getLong("messageId");
        if (messageId != null) {
            searchVo.setMessageId(messageId);
            messageMapper.updateMessageUserIsReadByUserUuidAndMessageId(searchVo);
        } else {
            List<Long> messageIdList = (List<Long>) jsonObj.get("messageIdList");
            if (CollectionUtils.isNotEmpty(messageIdList)) {
                searchVo.setMessageIdList(messageIdList);
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
                    String messageType = jsonObj.getString("messageType");
                    if (StringUtils.isNotBlank(messageType)) {
                        searchVo.setTriggerList(NotifyPolicyHandlerFactory.getTriggerList(messageType));
                        messageMapper.updateMessageUserIsReadByUserUuidAndTriggerList(searchVo);
                    } else {
                        messageMapper.updateMessageUserIsReadByUserUuid(userUuid);
                    }
                }
            }
        }
        JSONObject resultObj = new JSONObject();
        searchVo = new MessageSearchVo();
        searchVo.setUserUuid(userUuid);
        int newCount = messageMapper.getMessageCount(searchVo);
        resultObj.put("newCount", newCount);
        return resultObj;
    }
}
