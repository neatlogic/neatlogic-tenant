package codedriver.module.tenant.api.message;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.message.constvalue.PopUpType;
import codedriver.framework.message.core.MessageHandlerFactory;
import codedriver.framework.message.dao.mapper.MessageMapper;
import codedriver.framework.message.dto.MessageHandlerVo;
import codedriver.framework.message.dto.MessageSearchVo;
import codedriver.framework.message.dto.MessageVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Title: MessagePopUpListApi
 * @Package codedriver.module.tenant.api.message
 * @Description: 查询消息弹窗列表接口
 * @Author: linbq
 * @Date: 2021/2/20 17:11
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class MessagePopUpListApi extends PrivateApiComponentBase {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public String getToken() {
        return "message/popup/list";
    }

    @Override
    public String getName() {
        return "查询消息弹窗列表";
    }

    @Override
    public String getConfig() {
        return null;
    }
    @Input({
            @Param(name = "messageId", type = ApiParamType.LONG, isRequired = true, desc = "起点消息id"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条数")
    })
    @Output({
            @Param(name = "tbodyList", explode = MessageVo[].class, desc = "消息列表"),
            @Param(name = "newCount", type = ApiParamType.INTEGER, desc = "新消息总数"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "查询消息弹窗列表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        List<MessageVo> messageList = new ArrayList<>();
        MessageSearchVo searchVo = JSONObject.toJavaObject(jsonObj, MessageSearchVo.class);
        searchVo.setCurrentPage(1);
        searchVo.setUserUuid(UserContext.get().getUserUuid(true));
        int rowNum = messageMapper.getMessagePopUpCount(searchVo);
        if(rowNum > 0){
            List<MessageHandlerVo> messageSubscribeList = messageMapper.getMessageSubscribeListByUserUuid(UserContext.get().getUserUuid(true));
            Map<String, MessageHandlerVo> messageSubscribeMap = messageSubscribeList.stream().collect(Collectors.toMap(e -> e.getHandler(), e -> e));
            while(true){
                List<MessageVo> messageVoList = messageMapper.getMessagePopUpList(searchVo);
                if(CollectionUtils.isEmpty(messageVoList)){
                    break;
                }
                int closeCount = 0;
                List<Long> messageIdList = new ArrayList<>(messageVoList.size());
                for (MessageVo messageVo : messageVoList) {
                    MessageHandlerVo messageHandlerVo = messageSubscribeMap.get(messageVo.getHandler());
                    if (messageHandlerVo != null) {
                        messageVo.setPopUp(messageHandlerVo.getPopUp());
                        if(messageHandlerVo.getPopUp().equals(PopUpType.LONGSHOW.getValue())){
                            messageList.add(messageVo);
                        }else if(messageHandlerVo.getPopUp().equals(PopUpType.SHORTSHOW.getValue())){
                            messageList.add(messageVo);
                            messageIdList.add(messageVo.getId());
                        }else if(messageHandlerVo.getPopUp().equals(PopUpType.CLOSE.getValue())){
                            messageIdList.add(messageVo.getId());
                            closeCount++;
                        }
                    } else {
                        messageVo.setPopUp(PopUpType.CLOSE.getValue());
                        messageIdList.add(messageVo.getId());
                        closeCount++;
                    }
                }
                if(CollectionUtils.isNotEmpty(messageIdList)){
                    messageMapper.updateMessageUserIsRead(UserContext.get().getUserUuid(true), messageIdList);
                }
                if(closeCount == 0){
                    break;
                }
                rowNum -= closeCount;
                searchVo.setMessageId(messageVoList.get(messageVoList.size() - 1).getId());
            }
        }
        if(CollectionUtils.isEmpty(messageList)){
            rowNum = 0;
        }
        int pageCount = PageUtil.getPageCount(rowNum, searchVo.getPageSize());
        resultObj.put("currentPage", searchVo.getCurrentPage());
        resultObj.put("pageSize", searchVo.getPageSize());
        resultObj.put("pageCount", pageCount);
        resultObj.put("rowNum", rowNum);
        resultObj.put("tbodyList", messageList);
        return resultObj;
    }
}
