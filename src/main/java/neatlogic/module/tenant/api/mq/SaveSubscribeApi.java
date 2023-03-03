/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.mq;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.MQ_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.mq.SubscribeHandlerNotFoundException;
import neatlogic.framework.exception.mq.SubscribeNameIsExistsException;
import neatlogic.framework.exception.mq.SubscribeNotFoundException;
import neatlogic.framework.mq.core.ISubscribeHandler;
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
import com.alibaba.fastjson.JSONObject;
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
            @Param(name = "isDurable", isRequired = true, type = ApiParamType.INTEGER, desc = "是否持久订阅"),
            @Param(name = "description", xss = true, type = ApiParamType.STRING, desc = "说明"),
            @Param(name = "isActive", isRequired = true, type = ApiParamType.INTEGER, desc = "是否激活"),
            @Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "配置")})
    @Description(desc = "保存消息队列订阅接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        SubscribeVo subscribeVo = JSONObject.toJavaObject(jsonObj, SubscribeVo.class);
        subscribeVo.setError("");
        ISubscribeHandler subscribeHandler = SubscribeHandlerFactory.getHandler(subscribeVo.getClassName());
        if (subscribeHandler == null) {
            throw new SubscribeHandlerNotFoundException(subscribeVo.getClassName());
        }
        if (mqSubscribeMapper.checkSubscribeNameIsExists(subscribeVo) > 0) {
            throw new SubscribeNameIsExistsException(subscribeVo.getName());
        }
        if (jsonObj.getLong("id") != null) {
            SubscribeVo oldSubVo = mqSubscribeMapper.getSubscribeById(jsonObj.getLong("id"));
            if (oldSubVo == null) {
                throw new SubscribeNotFoundException(jsonObj.getLong("id"));
            }
            SubscribeManager.destroy(oldSubVo.getTopicName(), oldSubVo.getName());
            subscribeVo.setTopicName(oldSubVo.getTopicName());
            subscribeVo.setName(oldSubVo.getName());
        }
        if (subscribeVo.getIsActive().equals(1)) {
            try {
                SubscribeManager.create(subscribeVo.getTopicName(), subscribeVo.getName(), subscribeVo.getIsDurable().equals(1), subscribeHandler);
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
