package codedriver.module.tenant.api.message;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.message.core.MessageHandlerFactory;
import codedriver.framework.message.dao.mapper.MessageMapper;
import codedriver.framework.message.dto.MessageHandlerVo;
import codedriver.framework.restful.core.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Title: MessageHandlerListApi
 * @Package codedriver.module.tenant.api.message
 * @Description: 查询消息类型列表接口
 * @Author: linbq
 * @Date: 2020/12/30 17:38
 * Copyright(c) 2020 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
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
        return resultList;
    }
}
