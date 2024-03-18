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
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
