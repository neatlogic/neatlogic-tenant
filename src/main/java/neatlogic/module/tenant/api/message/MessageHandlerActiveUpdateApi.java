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
import neatlogic.framework.message.constvalue.PopUpType;
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
public class MessageHandlerActiveUpdateApi extends PrivateApiComponentBase {

    @Autowired
    private MessageMapper messageMapper;

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
    @Output({
            @Param(name = "Return", type = ApiParamType.INTEGER, desc = "是否激活")
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
            messageMapper.updateMessageSubscribeActive(messageHandlerVo);
            return messageHandlerVo.getIsActive() == 0 ? 1 : 0;
        } else {
            messageHandlerVo = new MessageHandlerVo();
            messageHandlerVo.setHandler(handler);
            messageHandlerVo.setIsActive(0);
            messageHandlerVo.setPopUp(PopUpType.CLOSE.getValue());
            messageHandlerVo.setUserUuid(UserContext.get().getUserUuid(true));
            messageMapper.insertMessageSubscribe(messageHandlerVo);
            return 0;
        }
    }
}
