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

package neatlogic.module.tenant.api.message;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.message.core.IMessageHandler;
import neatlogic.framework.message.core.MessageHandlerFactory;
import neatlogic.framework.message.dao.mapper.MessageMapper;
import neatlogic.framework.message.dto.MessageHandlerVo;
import neatlogic.framework.message.exception.MessageHandlerNotFoundException;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
