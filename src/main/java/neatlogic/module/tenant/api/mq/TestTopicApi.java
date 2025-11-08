/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.api.mq;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.MQ_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.mq.MqHandlerNotFoundException;
import neatlogic.framework.exception.mq.TopicNotFoundException;
import neatlogic.framework.mq.core.IMqHandler;
import neatlogic.framework.mq.core.MqHandlerFactory;
import neatlogic.framework.mq.dao.mapper.MqTopicMapper;
import neatlogic.framework.mq.dto.TopicVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@AuthAction(action = MQ_MODIFY.class)
@OperationType(type = OperationTypeEnum.OPERATE)
@Transactional
public class TestTopicApi extends PrivateApiComponentBase {

    @Resource
    private MqTopicMapper mqTopicMapper;


    @Override
    public String getToken() {
        return "/mq/topic/test";
    }

    @Override
    public String getName() {
        return "测试消息队列主题";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "name", isRequired = true, type = ApiParamType.STRING, desc = "主题唯一标识"),
            @Param(name = "content", isRequired = true, type = ApiParamType.STRING, desc = "内容")})
    @Description(desc = "测试消息队列主题")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String name = jsonObj.getString("name");
        String content = jsonObj.getString("content");
        TopicVo topicVo = mqTopicMapper.getTopicByName(name);
        if (topicVo == null) {
            throw new TopicNotFoundException(name);
        }
        IMqHandler mqHandler = MqHandlerFactory.getMqHandler(topicVo.getHandler());
        if (mqHandler == null) {
            throw new MqHandlerNotFoundException(topicVo.getHandler());
        }
        mqHandler.send(name, content);
        return null;
    }

}
