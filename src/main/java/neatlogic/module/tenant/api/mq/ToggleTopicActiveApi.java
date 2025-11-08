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
import neatlogic.framework.exception.mq.TopicNotFoundException;
import neatlogic.framework.mq.core.TopicFactory;
import neatlogic.framework.mq.dao.mapper.MqTopicMapper;
import neatlogic.framework.mq.dto.TopicVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = MQ_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class ToggleTopicActiveApi extends PrivateApiComponentBase {

    @Resource
    private MqTopicMapper mqTopicMapper;

    @Override
    public String getToken() {
        return "/mq/topic/toggleactive";
    }

    @Override
    public String getName() {
        return "term.framework.togglemqtopicisactive";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "name", isRequired = true, type = ApiParamType.STRING, desc = "common.uniquename"),
            @Param(name = "isActive", isRequired = true, type = ApiParamType.INTEGER, desc = "common.isactive")})
    @Description(desc = "term.framework.togglemqtopicisactive")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String name = jsonObj.getString("name");
        TopicVo topicVo = TopicFactory.getTopicByName(name);
        Integer isActive = jsonObj.getInteger("isActive");
        if (topicVo == null) {
            topicVo = mqTopicMapper.getTopicByName(name);
            if (topicVo == null) {
                throw new TopicNotFoundException(name);
            }
        }
        topicVo.setIsActive(isActive);
        mqTopicMapper.saveTopicIsActive(topicVo);
        return null;
    }

}
