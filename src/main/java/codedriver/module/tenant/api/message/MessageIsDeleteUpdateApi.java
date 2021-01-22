package codedriver.module.tenant.api.message;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.message.dao.mapper.MessageMapper;
import codedriver.framework.message.dto.MessageSearchVo;
import codedriver.framework.message.dto.MessageVo;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class MessageIsDeleteUpdateApi extends PrivateApiComponentBase {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public String getToken() {
        return "message/isdelete/update";
    }

    @Override
    public String getName() {
        return "更新消息为已删除";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "messageId", type = ApiParamType.LONG, desc = "消息id"),
            @Param(name = "messageIdList", type = ApiParamType.JSONARRAY, desc = "消息id列表"),
            @Param(name = "date", type = ApiParamType.LONG, desc = "日期")
    })
    @Output({
            @Param(name = "newCount", type = ApiParamType.INTEGER, desc = "新消息总数")
    })
    @Description(desc = "更新消息为已删除")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String userUuid = UserContext.get().getUserUuid(true);
        Long messageId = jsonObj.getLong("messageId");
        if (messageId != null) {
            messageMapper.updateMessageUserIsDeleteByUserUuidAndMessageId(userUuid, messageId);
        } else {
            List<Long> messageIdList = (List<Long>) jsonObj.get("messageIdList");
            if (CollectionUtils.isNotEmpty(messageIdList)) {
                messageMapper.updateMessageUserIsDeleteByUserUuidAndMessageIdList(userUuid, messageIdList);
            } else {
                Date date = jsonObj.getDate("date");
                if (date != null) {
                    Long fromMessageId = messageMapper.getMessageMaxIdByLessThanFcd(date);
                    Date nextDay = new Date(date.getTime() + TimeUnit.HOURS.toMillis(24));
                    Long toMessageId = messageMapper.getMessageMaxIdByLessThanFcd(nextDay);
                    messageMapper.updateMessageUserIsDeleteByUserUuidAndMessageIdRange(userUuid, fromMessageId, toMessageId);
                } else {
                    messageMapper.updateMessageUserIsDeleteByUserUuid(userUuid);
                }
            }
        }
        JSONObject resultObj = new JSONObject();
        MessageSearchVo searchVo = new MessageSearchVo();
        searchVo.setUserUuid(userUuid);
        int newCount = messageMapper.getMessageNewCount(searchVo);
        resultObj.put("newCount", newCount);
        return null;
    }
}
