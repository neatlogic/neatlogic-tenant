package codedriver.module.tenant.api.message;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.message.constvalue.PopUpType;
import codedriver.framework.message.core.IMessageHandler;
import codedriver.framework.message.core.MessageHandlerFactory;
import codedriver.framework.message.dao.mapper.MessageMapper;
import codedriver.framework.message.dto.MessageHandlerVo;
import codedriver.framework.message.dto.MessageSearchVo;
import codedriver.framework.message.exception.MessageHandlerNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.service.message.MessageService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @Title: MessageHandlerActiveUpdateApi
 * @Package codedriver.module.tenant.api.message
 * @Description: 消息类型订阅接口
 * @Author: linbq
 * @Date: 2020/12/31 14:54
 **/
@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@Transactional
public class MessageHandlerActiveUpdateApi extends PrivateApiComponentBase {

    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private MessageService messageService;

    @Override
    public String getToken() {
        return "message/handler/active/update";
    }

    @Override
    public String getName() {
        return "消息类型订阅";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "handler", type = ApiParamType.STRING, isRequired = true, desc = "消息类型处理器全类名")
    })
    @Description(desc = "消息类型订阅")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String handler = jsonObj.getString("handler");
        IMessageHandler messageHandler = MessageHandlerFactory.getHandler(handler);
        if (messageHandler == null) {
            throw new MessageHandlerNotFoundException(handler);
        }
        MessageHandlerVo searchVo = new MessageHandlerVo();
        searchVo.setHandler(handler);
        searchVo.setUserUuid(UserContext.get().getUserUuid(true));
        MessageHandlerVo messageHandlerVo = messageMapper.getMessageSubscribeByUserUuidAndHandler(searchVo);
        if (messageHandlerVo != null) {
            messageHandlerVo.setUserUuid(UserContext.get().getUserUuid(true));
            messageMapper.updateSubscribeActive(messageHandlerVo);
            if(messageHandlerVo.getIsActive() == 1){
                pullMessage(handler);
            }
        } else {
            messageHandlerVo = new MessageHandlerVo();
            messageHandlerVo.setHandler(handler);
            messageHandlerVo.setIsActive(0);
            messageHandlerVo.setPopUp(PopUpType.CLOSE.getValue());
            messageHandlerVo.setUserUuid(UserContext.get().getUserUuid(true));
            messageMapper.insertSubscribe(messageHandlerVo);
            pullMessage(handler);
        }
        return null;
    }

    private void pullMessage(String handler) {
        MessageSearchVo messageSearchVo = new MessageSearchVo();
        messageSearchVo.setNeedPage(false);
        List<String> handlerList = new ArrayList<>();
        handlerList.add(handler);
        messageSearchVo.setHandlerList(handlerList);
        messageService.pullMessage(messageSearchVo);
    }
}