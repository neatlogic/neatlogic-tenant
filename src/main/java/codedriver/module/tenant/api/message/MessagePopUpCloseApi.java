package codedriver.module.tenant.api.message;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.message.dao.mapper.MessageMapper;
import codedriver.framework.message.dto.MessageSearchVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Title: MessagePopUpCloseApi
 * @Package codedriver.module.tenant.api.message
 * @Description: 关闭弹窗接口
 * @Author: linbq
 * @Date: 2021/2/24 19:55
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
@Service
@OperationType(type = OperationTypeEnum.UPDATE)
public class MessagePopUpCloseApi extends PrivateApiComponentBase {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public String getToken() {
        return "message/popup/close";
    }

    @Override
    public String getName() {
        return "关闭弹窗";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "messageId", type = ApiParamType.LONG, desc = "单个消息id"),
            @Param(name = "maxMessageId", type = ApiParamType.LONG, desc = "最大消息id")
    })
    @Description(desc = "关闭弹窗")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String userUuid = UserContext.get().getUserUuid(true);
        /** 将is_show=1,expired_time>=NOW(3) （临时弹窗且已自动消失）的消息改成 is_show = 2, expired_time = null **/
        messageMapper.updateMessageUserExpiredIsShow1To2AndExpiredTimeIsNullByUserUuid(userUuid);
        MessageSearchVo searchVo = new MessageSearchVo();
        searchVo.setUserUuid(userUuid);
        Long messageId = jsonObj.getLong("messageId");
        if(messageId != null){
            searchVo.setMessageId(messageId);
            messageMapper.updateMessageUserIsShow1To2AndIsRead0To1ByUserUuidAndMessageId(searchVo);
        }else {
            Long maxMessageId = jsonObj.getLong("maxMessageId");
            if(maxMessageId != null){
                searchVo.setMaxMessageId(maxMessageId);
                messageMapper.updateMessageUserIsShow1To2AndIsRead0To1ByUserUuidAndMessageId(searchVo);
            }
        }
        return null;
    }
}
