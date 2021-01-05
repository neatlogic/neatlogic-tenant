package codedriver.module.tenant.api.message;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.message.constvalue.PopUpType;
import codedriver.framework.message.core.MessageHandlerFactory;
import codedriver.framework.message.dao.mapper.MessageMapper;
import codedriver.framework.message.dto.MessageHandlerVo;
import codedriver.framework.message.dto.MessageSearchVo;
import codedriver.framework.message.dto.MessageVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.service.message.MessageService;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Title: NewsMessagePullApi
 * @Package codedriver.module.tenant.api.news
 * @Description: 拉取新消息列表接口
 * @Author: linbq
 * @Date: 2021/1/4 15:13
 **/
@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@Transactional
public class MessagePullApi extends PrivateApiComponentBase {

    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private MessageService messageService;

    @Override
    public String getToken() {
        return "message/pull";
    }

    @Override
    public String getName() {
        return "拉取新消息列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条数")
    })
    @Output({
            @Param(name = "tbodyList", explode = MessageVo[].class, desc = "消息列表"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "拉取新消息列表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        resultObj.put("tbodyList", new ArrayList<>());
        MessageSearchVo searchVo = JSONObject.toJavaObject(jsonObj, MessageSearchVo.class);
        Map<String, MessageHandlerVo> newsSubscribeMap = new HashMap<>();
        List<String> unActiveHandlerList = new ArrayList<>();
        List<MessageHandlerVo> newsSubscribeList = messageMapper.getMessageSubscribeListByUserUuid(UserContext.get().getUserUuid(true));
        for (MessageHandlerVo newsSubscribe : newsSubscribeList) {
            newsSubscribeMap.put(newsSubscribe.getHandler(), newsSubscribe);
            if(newsSubscribe.getIsActive() == 0){
                unActiveHandlerList.add(newsSubscribe.getHandler());
            }
        }
        if(CollectionUtils.isNotEmpty(unActiveHandlerList)){
            List<String> handlerList = MessageHandlerFactory.getMessageHandlerVoList().stream().map(MessageHandlerVo::getHandler).collect(Collectors.toList());
            handlerList.removeAll(unActiveHandlerList);
            searchVo.setHandlerList(handlerList);
        }
        List<Long> newsMessageIdList = messageService.pullMessage(searchVo);
        resultObj.put("currentPage", searchVo.getCurrentPage());
        resultObj.put("pageSize", searchVo.getPageSize());
        resultObj.put("rowNum", searchVo.getRowNum());
        resultObj.put("pageCount", searchVo.getPageCount());
        if(CollectionUtils.isNotEmpty(newsMessageIdList)){
            List<MessageVo> messageVoList = messageMapper.getMessageListByIdList(newsMessageIdList);
            for(MessageVo messageVo : messageVoList){
                messageVo.setIsRead(0);
                MessageHandlerVo messageHandlerVo = newsSubscribeMap.get(messageVo.getHandler());
                if(messageHandlerVo != null){
                    messageVo.setPopUp(messageHandlerVo.getPopUp());
                }else{
                    messageVo.setPopUp(PopUpType.CLOSE.getValue());
                }
            }
            resultObj.put("tbodyList", messageVoList);
        }
        return resultObj;
    }
}
