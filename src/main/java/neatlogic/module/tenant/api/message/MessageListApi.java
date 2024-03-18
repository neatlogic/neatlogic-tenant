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
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.message.core.MessageHandlerFactory;
import neatlogic.framework.message.dao.mapper.MessageMapper;
import neatlogic.framework.message.dto.MessageHandlerVo;
import neatlogic.framework.message.dto.MessageSearchVo;
import neatlogic.framework.message.dto.MessageVo;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
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
     * @Returns:java.util.List<neatlogic.framework.message.dto.MessageVo>
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
