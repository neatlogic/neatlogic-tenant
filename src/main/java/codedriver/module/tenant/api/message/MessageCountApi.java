package codedriver.module.tenant.api.message;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.asynchronization.threadpool.CommonThreadPool;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.FRAMEWORK_BASE;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.message.constvalue.PopUpType;
import codedriver.framework.message.core.MessageHandlerFactory;
import codedriver.framework.message.dao.mapper.MessageMapper;
import codedriver.framework.message.dto.MessageHandlerVo;
import codedriver.framework.message.dto.MessageSearchVo;
import codedriver.framework.message.dto.MessageVo;
import codedriver.framework.message.thread.UpdateMessageUserIsShowThread;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Title: MessagePull2Api
 * @Package codedriver.module.tenant.api.message
 * @Description: 查询消息数量接口
 * @Author: linbq
 * @Date: 2021/2/24 12:03
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
@Service
@AuthAction(action = FRAMEWORK_BASE.class)
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
        if(popUpCount > 0){
            int shortShowTime = jsonObj.getIntValue("shortShowTime");
            /** 计算临时弹窗失效时间 **/
            Date expiredTime = null;
            if(shortShowCount != 0){
                expiredTime = new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(shortShowTime));
            }
            /** 异步处理消息状态is_show **/
            CommonThreadPool.execute(new UpdateMessageUserIsShowThread(
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
