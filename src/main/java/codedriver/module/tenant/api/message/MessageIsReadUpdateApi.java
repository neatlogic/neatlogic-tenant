package codedriver.module.tenant.api.message;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.message.dao.mapper.MessageMapper;
import codedriver.framework.message.dto.MessageSearchVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Title: MessageIsReadUpdateApi
 * @Package codedriver.module.tenant.api.message
 * @Description: 更新消息为已读接口
 * @Author: linbq
 * @Date: 2021/1/4 23:12
 **/
@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@Transactional
public class MessageIsReadUpdateApi extends PrivateApiComponentBase {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public String getToken() {
        return "message/isread/update";
    }

    @Override
    public String getName() {
        return "更新消息为已读";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "messageIdList", type = ApiParamType.JSONARRAY, desc = "消息id列表")
    })
    @Description(desc = "更新消息为已读")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<Long> messageIdList = (List<Long>)jsonObj.get("messageIdList");
        messageMapper.updateMessageUserIsRead(UserContext.get().getUserUuid(true), messageIdList);
        return null;
    }
}
