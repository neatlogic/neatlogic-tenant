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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.MQ_MODIFY;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.mq.SubscribeHandlerIsEmbedException;
import neatlogic.framework.exception.mq.SubscribeIsSystemException;
import neatlogic.framework.exception.mq.SubscribeNameIsExistsException;
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
@OperationType(type = OperationTypeEnum.UPDATE)
@Transactional
public class SaveSubscribeApi extends PrivateApiComponentBase {

    @Resource
    private MqSubscribeMapper mqSubscribeMapper;

    @Override
    public String getToken() {
        return "/mq/subscribe/save";
    }

    @Override
    public String getName() {
        return "保存消息队列订阅";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "id"),
            @Param(name = "name", isRequired = true, type = ApiParamType.STRING, desc = "唯一标识"),
            @Param(name = "className", isRequired = true, type = ApiParamType.STRING, desc = "处理类名"),
            @Param(name = "topicName", isRequired = true, type = ApiParamType.STRING, desc = "主题名"),
            // @Param(name = "isDurable", isRequired = true, type = ApiParamType.INTEGER, desc = "是否持久订阅"),
            @Param(name = "description", xss = true, type = ApiParamType.STRING, desc = "说明"),
            @Param(name = "isActive", isRequired = true, type = ApiParamType.INTEGER, desc = "是否激活"),
            @Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "配置")})
    @Description(desc = "保存消息队列订阅")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        SubscribeVo subscribeVo = JSON.toJavaObject(jsonObj, SubscribeVo.class);
        subscribeVo.setServerId(Config.SCHEDULE_SERVER_ID);
        subscribeVo.setError("");
        if (mqSubscribeMapper.checkSubscribeNameIsExists(subscribeVo) > 0) {
            throw new SubscribeNameIsExistsException(subscribeVo.getName());
        }
        Long id = jsonObj.getLong("id");
        SubscribeVo oldSubVo = null;
        if (id != null) {
            oldSubVo = mqSubscribeMapper.getSubscribeById(id);
            if (oldSubVo == null) {
                throw new SubscribeNotFoundException(id);
            }
            if (SubscribeHandlerFactory.hasSystemSubscribe(oldSubVo.getName())) {
                throw new SubscribeIsSystemException(oldSubVo.getName());
            }
        }
        if (SubscribeHandlerFactory.isEmbedSubscribeHandler(subscribeVo.getClassName())) {
            throw new SubscribeHandlerIsEmbedException(subscribeVo.getClassName());
        }
        if (oldSubVo != null) {
            SubscribeManager.destroy(oldSubVo);
        }
        if (subscribeVo.getIsActive().equals(1)) {
            try {
                SubscribeManager.create(subscribeVo);
            } catch (Exception ex) {
                subscribeVo.setError(ex.getMessage());
            }
        }
        if (jsonObj.getLong("id") == null) {
            mqSubscribeMapper.insertSubscribe(subscribeVo);
        } else {
            mqSubscribeMapper.updateSubscribe(subscribeVo);
        }

        return null;
    }

}
