/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.message;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.message.dao.mapper.MessageMapper;
import codedriver.framework.message.dto.MessageSearchVo;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
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
