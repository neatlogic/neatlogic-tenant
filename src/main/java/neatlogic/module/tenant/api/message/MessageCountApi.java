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
import neatlogic.framework.asynchronization.threadpool.CachedThreadPool;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.message.constvalue.PopUpType;
import neatlogic.framework.message.core.MessageHandlerFactory;
import neatlogic.framework.message.dao.mapper.MessageMapper;
import neatlogic.framework.message.dto.MessageHandlerVo;
import neatlogic.framework.message.dto.MessageSearchVo;
import neatlogic.framework.message.dto.MessageVo;
import neatlogic.framework.message.thread.UpdateMessageUserIsShowThread;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service

@OperationType(type = OperationTypeEnum.UPDATE)
public class MessageCountApi extends PrivateApiComponentBase {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public String getToken() {
        return "message/count";
    }

    @Override
    public String getName() {
        return "查询消息数量";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({
            @Param(name = "message", explode = MessageVo.class, desc = "弹窗消息内容"),
            @Param(name = "unreadCount", type = ApiParamType.INTEGER, desc = "未读消息数量"),
            @Param(name = "shortShowCount", type = ApiParamType.INTEGER, desc = "持续显示消息数量"),
            @Param(name = "LongShowCount", type = ApiParamType.INTEGER, desc = "临时显示消息数量"),
            @Param(name = "shortShowTime", type = ApiParamType.INTEGER, isRequired = true, desc = "临时显示时间,单位秒"),
    })
    @Description(desc = "查询消息数量")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        List<MessageHandlerVo> messageHandlerVoList = MessageHandlerFactory.getMessageHandlerVoList();
        List<String> popUpCloseHandlerList = messageHandlerVoList.stream().map(MessageHandlerVo::getHandler).collect(Collectors.toList());
        List<String> popUpShortShowHandlerList = new ArrayList<>();
        List<String> popUpLongShowHandlerList = new ArrayList<>();
        String userUuid = UserContext.get().getUserUuid(true);
        List<MessageHandlerVo> messageSubscribeList = messageMapper.getMessageSubscribeListByUserUuid(userUuid);
        for(MessageHandlerVo messageHandlerVo : messageSubscribeList){
            if(messageHandlerVo.getPopUp().equals(PopUpType.LONGSHOW.getValue())){
                popUpLongShowHandlerList.add(messageHandlerVo.getHandler());
                popUpCloseHandlerList.remove(messageHandlerVo.getHandler());
            }else if(messageHandlerVo.getPopUp().equals(PopUpType.SHORTSHOW.getValue())){
                popUpShortShowHandlerList.add(messageHandlerVo.getHandler());
                popUpCloseHandlerList.remove(messageHandlerVo.getHandler());
            }
        }
        MessageSearchVo messageSearchVo = new MessageSearchVo();
        messageSearchVo.setUserUuid(userUuid);
        int shortShowCount = 0;
        if(CollectionUtils.isNotEmpty(popUpShortShowHandlerList)){
            messageSearchVo.setHandlerList(popUpShortShowHandlerList);
            shortShowCount = messageMapper.getMessageShortShowPopUpCountByUserUuidAndHandlerList(messageSearchVo);
        }
        int longShowCount = 0;
        if(CollectionUtils.isNotEmpty(popUpLongShowHandlerList)){
            messageSearchVo.setHandlerList(popUpLongShowHandlerList);
            longShowCount = messageMapper.getMessageLongShowPopUpCountByUserUuidAndHandlerList(messageSearchVo);
        }
        int popUpCount = shortShowCount + longShowCount;
        if(popUpCount > 0){
            List<String> needPopUpHandlerList = new ArrayList<>(popUpShortShowHandlerList.size() + popUpLongShowHandlerList.size());
            needPopUpHandlerList.addAll(popUpShortShowHandlerList);
            needPopUpHandlerList.addAll(popUpLongShowHandlerList);
            messageSearchVo.setHandlerList(needPopUpHandlerList);
            MessageVo messageVo = messageMapper.getLastPopUpMessage(messageSearchVo);
            if(messageVo != null){
                if(popUpShortShowHandlerList.contains(messageVo.getHandler())){
                    messageVo.setPopUp(PopUpType.SHORTSHOW.getValue());
                }else if(popUpLongShowHandlerList.contains(messageVo.getHandler())){
                    messageVo.setPopUp(PopUpType.LONGSHOW.getValue());
                }
                resultObj.put("message", messageVo);
            }
        }
        int unreadCount = 0;
        if(popUpCount < 100){
            unreadCount = messageMapper.getMessageUnreadCountByUserUuid(userUuid);
        }else{
            unreadCount = popUpCount;
        }
        resultObj.put("unreadCount", unreadCount);
        resultObj.put("shortShowCount", shortShowCount);
        resultObj.put("longShowCount", longShowCount);
        if (popUpCount > 0) {
            int shortShowTime = jsonObj.getIntValue("shortShowTime");
            /* 计算临时弹窗失效时间 **/
            Date expiredTime = null;
            if (shortShowCount != 0) {
                expiredTime = new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(shortShowTime));
            }
            /* 异步处理消息状态is_show **/
            CachedThreadPool.execute(new UpdateMessageUserIsShowThread(
                    shortShowCount,
                    longShowCount,
                    popUpShortShowHandlerList,
                    popUpLongShowHandlerList,
                    popUpCloseHandlerList,
                    expiredTime)
            );
        }

        return resultObj;
    }
}
