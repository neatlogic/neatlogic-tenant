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
import neatlogic.framework.exception.mq.SubscribeIsSystemException;
import neatlogic.framework.exception.mq.SubscribeNotFoundException;
import neatlogic.framework.mq.core.SubscribeHandlerFactory;
import neatlogic.framework.mq.core.SubscribeManager;
import neatlogic.framework.mq.dao.mapper.MqSubscribeMapper;
import neatlogic.framework.mq.dto.SubscribeVo;
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
@OperationType(type = OperationTypeEnum.DELETE)
@Transactional
public class DeleteSubscribeApi extends PrivateApiComponentBase {

    @Resource
    private MqSubscribeMapper mqSubscribeMapper;

    @Override
    public String getToken() {
        return "/mq/subscribe/delete";
    }

    @Override
    public String getName() {
        return "删除消息队列订阅";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", isRequired = true, type = ApiParamType.LONG, desc = "id")})
    @Description(desc = "删除消息队列订阅")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        SubscribeVo subVo = mqSubscribeMapper.getSubscribeById(jsonObj.getLong("id"));
        if (subVo == null) {
            throw new SubscribeNotFoundException(jsonObj.getLong("id"));
        }
        if (SubscribeHandlerFactory.hasSystemSubscribe(subVo.getName())) {
            throw new SubscribeIsSystemException(subVo.getName());
        }
        mqSubscribeMapper.deleteSubscribeById(jsonObj.getLong("id"));
        SubscribeManager.destroy(subVo);
        return null;
    }

}
