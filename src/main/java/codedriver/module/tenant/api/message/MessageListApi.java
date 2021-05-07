/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.message;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.message.core.MessageHandlerFactory;
import codedriver.framework.message.dao.mapper.MessageMapper;
import codedriver.framework.message.dto.MessageHandlerVo;
import codedriver.framework.message.dto.MessageSearchVo;
import codedriver.framework.message.dto.MessageVo;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class MessageListApi extends PrivateApiComponentBase {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public String getToken() {
        return "message/list";
    }
    @Override
    public String getName() {
        return "查询消息列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "messageId", type = ApiParamType.LONG, desc = "单个消息id"),
            @Param(name = "minMessageId", type = ApiParamType.LONG, desc = "最小消息id"),
            @Param(name = "maxMessageId", type = ApiParamType.LONG, desc = "最大消息id"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条数")
    })
    @Output({
            @Param(name = "tbodyList", explode = MessageVo[].class, desc = "消息列表"),
            @Param(name = "hasSubscription", type = ApiParamType.INTEGER, desc = "是否有订阅消息"),
            @Param(name = "unreadCount", type = ApiParamType.INTEGER, desc = "未读消息数量"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "查询消息列表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        String userUuid = UserContext.get().getUserUuid(true);
        MessageSearchVo searchVo = new MessageSearchVo();
        searchVo.setUserUuid(userUuid);
        List<MessageVo> messageVoList = null;
        Long messageId = jsonObj.getLong("messageId");
        if(messageId != null){
            /** 查询单个消息内容 **/
            searchVo.setMessageId(messageId);
            MessageVo messageVo = messageMapper.getMessageByIdAndUserUuid(searchVo);
            messageVoList = new ArrayList<>();
            resultObj.put("currentPage", 1);
            resultObj.put("pageSize", 1);
            if(messageVo != null){
                messageVoList.add(messageVo);
                resultObj.put("pageCount", 1);
                resultObj.put("rowNum", 1);
            }else{
                resultObj.put("pageCount", 0);
                resultObj.put("rowNum", 0);
            }
        }else {
            Integer pageSize = jsonObj.getInteger("pageSize");
            if(pageSize != null){
                searchVo.setPageSize(pageSize);
            }
            Long maxMessageId = jsonObj.getLong("maxMessageId");
            if(maxMessageId != null){
                /** 查询下一页 **/
                searchVo.setMaxMessageId(maxMessageId);
            }else {
                Long minMessageId = jsonObj.getLong("minMessageId");
                if(minMessageId != null){
                    /** 查询上一页 **/
                    searchVo.setMinMessageId(minMessageId);
                }
            }
            messageVoList = getMessageVoList(searchVo);

            resultObj.put("currentPage", searchVo.getCurrentPage());
            resultObj.put("pageSize", searchVo.getPageSize());
            resultObj.put("pageCount", searchVo.getPageCount());
            resultObj.put("rowNum", searchVo.getRowNum());
        }
        resultObj.put("tbodyList", messageVoList);

        searchVo = new MessageSearchVo();
        searchVo.setUserUuid(userUuid);
        int unreadCount = messageMapper.getMessageCount(searchVo);
        resultObj.put("unreadCount", unreadCount);

        List<String> unsubscribeHandlerList = new ArrayList<>();
        List<MessageHandlerVo> messageSubscribeList = messageMapper.getMessageSubscribeListByUserUuid(UserContext.get().getUserUuid(true));
        for(MessageHandlerVo messageHandlerVo : messageSubscribeList){
            if(messageHandlerVo.getIsActive() == 1){
                resultObj.put("hasSubscription", 1);
                return resultObj;
            }
            unsubscribeHandlerList.add(messageHandlerVo.getHandler());
        }
        for (MessageHandlerVo messageHandlerVo : MessageHandlerFactory.getMessageHandlerVoList()) {
            if(!unsubscribeHandlerList.contains(messageHandlerVo.getHandler())){
                resultObj.put("hasSubscription", 1);
                return resultObj;
            }
        }
        resultObj.put("hasSubscription", 0);
        return resultObj;
    }
    /**
     * @Description: 分页查询抽屉列表
     * @Author: linbq
     * @Date: 2021/2/24 19:46
     * @Params:[searchVo]
     * @Returns:java.util.List<codedriver.framework.message.dto.MessageVo>
     **/
    private List<MessageVo> getMessageVoList(MessageSearchVo searchVo){
        searchVo.setCurrentPage(1);
        int pageCount = 0;
        int rowNum = messageMapper.getMessageCount(searchVo);
        if(rowNum > 0){
            List<MessageVo> messageVoList = messageMapper.getMessageList(searchVo);
            if(CollectionUtils.isNotEmpty(messageVoList)){
                pageCount = PageUtil.getPageCount(rowNum, searchVo.getPageSize());
                searchVo.setPageCount(pageCount);
            }else{
                rowNum = 0;
            }
            searchVo.setRowNum(rowNum);
            return messageVoList;
        }
        return null;
    }
}
