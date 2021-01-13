package codedriver.module.tenant.api.message;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.message.core.IMessageHandler;
import codedriver.framework.message.core.MessageHandlerFactory;
import codedriver.framework.message.dao.mapper.MessageMapper;
import codedriver.framework.message.dto.MessageHandlerVo;
import codedriver.framework.message.exception.MessageHandlerNotFoundException;
import codedriver.framework.restful.core.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Title: MessageHandlerPopUpUpdateApi
 * @Package codedriver.module.tenant.api.message
 * @Description: 更新消息类型桌面推送方式接口
 * @Author: linbq
 * @Date: 2020/12/31 14:55
 * Copyright(c) 2020 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@Transactional
public class MessageHandlerPopUpUpdateApi extends PrivateApiComponentBase {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public String getToken() {
        return "message/handler/popup/update";
    }

    @Override
    public String getName() {
        return "更新消息类型桌面推送方式";
    }

    @Override
    public String getConfig() {
        return null;
    }
    @Input({
            @Param(name = "handler", type = ApiParamType.STRING, isRequired = true, desc = "消息类型处理器全类名"),
            @Param(name = "popUp", type = ApiParamType.ENUM, rule = "shortshow,longshow,close", isRequired = true, desc = "桌面推送方式")
    })
    @Output({
            @Param(name = "Return", type = ApiParamType.STRING, desc = "桌面推送方式")
    })
    @Description(desc = "更新消息类型桌面推送方式")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String handler = jsonObj.getString("handler");
        IMessageHandler messageHandler = MessageHandlerFactory.getHandler(handler);
        if(messageHandler == null){
            throw new MessageHandlerNotFoundException(handler);
        }
        String popUp = jsonObj.getString("popUp");
        MessageHandlerVo searchVo = new MessageHandlerVo();
        searchVo.setHandler(handler);
        searchVo.setUserUuid(UserContext.get().getUserUuid(true));
        MessageHandlerVo messageHandlerVo = messageMapper.getMessageSubscribeByUserUuidAndHandler(searchVo);
        if(messageHandlerVo != null){
            messageHandlerVo.setPopUp(popUp);
            messageHandlerVo.setUserUuid(UserContext.get().getUserUuid(true));
            messageMapper.updateMessageSubscribePopUp(messageHandlerVo);
        }else{
            messageHandlerVo = new MessageHandlerVo();
            messageHandlerVo.setHandler(handler);
            messageHandlerVo.setIsActive(1);
            messageHandlerVo.setPopUp(popUp);
            messageHandlerVo.setUserUuid(UserContext.get().getUserUuid(true));
            messageMapper.insertMessageSubscribe(messageHandlerVo);
        }
        return popUp;
    }
}
