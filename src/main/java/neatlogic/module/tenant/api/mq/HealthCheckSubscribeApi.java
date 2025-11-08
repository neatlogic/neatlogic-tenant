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
import neatlogic.framework.exception.mq.SubscribeNotFoundException;
import neatlogic.framework.mq.core.IMqHandler;
import neatlogic.framework.mq.core.MqHandlerFactory;
import neatlogic.framework.mq.dao.mapper.MqSubscribeMapper;
import neatlogic.framework.mq.dto.HealthcheckResultVo;
import neatlogic.framework.mq.dto.SubscribeVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@AuthAction(action = MQ_MODIFY.class)
@OperationType(type = OperationTypeEnum.OPERATE)
@Transactional
public class HealthCheckSubscribeApi extends PrivateApiComponentBase {


    @Resource
    private MqSubscribeMapper subscribeMapper;


    @Override
    public String getToken() {
        return "/mq/subscribe/healthcheck";
    }

    @Override
    public String getName() {
        return "消息订阅健康检查";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", isRequired = true, type = ApiParamType.LONG, desc = "订阅id")})
    @Output({@Param(explode = HealthcheckResultVo[].class)})
    @Description(desc = "消息订阅健康检查")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        SubscribeVo subscribeVo = subscribeMapper.getSubscribeById(jsonObj.getLong("id"));
        if (subscribeVo == null) {
            throw new SubscribeNotFoundException(jsonObj.getString("id"));
        }
        IMqHandler mqHandler = MqHandlerFactory.getMqHandler(subscribeVo.getHandler());
        if (mqHandler == null) {
            throw new MqHandlerNotFoundException(subscribeVo.getHandler());
        }
        return mqHandler.healthCheck(subscribeVo);
    }

}
