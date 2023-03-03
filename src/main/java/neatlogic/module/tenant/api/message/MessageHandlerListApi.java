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
import neatlogic.framework.message.core.MessageHandlerFactory;
import neatlogic.framework.message.dao.mapper.MessageMapper;
import neatlogic.framework.message.dto.MessageHandlerVo;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class MessageHandlerListApi extends PrivateApiComponentBase {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public String getToken() {
        return "message/handler/list";
    }

    @Override
    public String getName() {
        return "查询消息类型列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "moduleId", type = ApiParamType.STRING, desc = "模块id")
    })
    @Output({
            @Param(explode = MessageHandlerVo[].class)
    })
    @Description(desc = "查询消息类型列表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String moduleId = jsonObj.getString("moduleId");
        List<MessageHandlerVo> resultList = new ArrayList<>();
        List<MessageHandlerVo> messageSubscribeList = messageMapper.getMessageSubscribeListByUserUuid(UserContext.get().getUserUuid(true));
        Map<String, MessageHandlerVo> messageSubscribeMap = messageSubscribeList.stream().collect(Collectors.toMap(e -> e.getHandler(), e -> e));
        for (MessageHandlerVo messageHandlerVo : MessageHandlerFactory.getMessageHandlerVoList()) {
            if(messageHandlerVo.isPublic()){
                if(StringUtils.isBlank(moduleId) || moduleId.equals(messageHandlerVo.getModuleId())){
                    MessageHandlerVo messageHandler = messageHandlerVo.clone();
                    MessageHandlerVo messageSubscribe = messageSubscribeMap.get(messageHandler.getHandler());
                    if (messageSubscribe != null) {
                        messageHandler.setIsActive(messageSubscribe.getIsActive());
                        messageHandler.setPopUp(messageSubscribe.getPopUp());
                    }
                    resultList.add(messageHandler);
                }
            }
        }
        return resultList;
    }
}
