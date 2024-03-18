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
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service

@OperationType(type = OperationTypeEnum.DELETE)
@Transactional
public class MessageDeleteApi extends PrivateApiComponentBase {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public String getToken() {
        return "message/delete";
    }

    @Override
    public String getName() {
        return "删除消息";
    }

    @Override
    public String getConfig() {
        return null;
    }
    @Input({
            @Param(name = "messageId", type = ApiParamType.LONG, isRequired = true, desc = "消息id")
    })
    @Description(desc = "删除消息")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long messageId = jsonObj.getLong("messageId");
        messageMapper.deleteMessageUser(new MessageSearchVo(UserContext.get().getUserUuid(true), messageId));
        return null;
    }
}
