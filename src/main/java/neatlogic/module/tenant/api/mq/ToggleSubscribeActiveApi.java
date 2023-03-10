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

import javax.annotation.Resource;

@Service
@AuthAction(action = MQ_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class ToggleSubscribeActiveApi extends PrivateApiComponentBase {

    @Resource
    private MqSubscribeMapper mqSubscribeMapper;

    @Override
    public String getToken() {
        return "/mq/subscribe/toggleactive";
    }

    @Override
    public String getName() {
        return "????????????????????????????????????";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", isRequired = true, type = ApiParamType.LONG, desc = "id"),
            @Param(name = "isActive", isRequired = true, type = ApiParamType.INTEGER, desc = "????????????")})
    @Description(desc = "??????????????????????????????????????????")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        SubscribeVo subscribeVo = JSONObject.toJavaObject(jsonObj, SubscribeVo.class);
        SubscribeVo checkVo = mqSubscribeMapper.getSubscribeById(subscribeVo.getId());
        if (checkVo == null) {
            throw new SubscribeNotFoundException(subscribeVo.getId());
        }
        checkVo.setIsActive(subscribeVo.getIsActive());

        SubscribeManager.destroy(checkVo.getTopicName(), checkVo.getName());
        if (checkVo.getIsActive().equals(1)) {
            ISubscribeHandler subscribeHandler = SubscribeHandlerFactory.getHandler(checkVo.getClassName());
            if (subscribeHandler == null) {
                throw new SubscribeHandlerNotFoundException(checkVo.getClassName());
            }
            try {
                SubscribeManager.create(checkVo.getTopicName(), checkVo.getName(), checkVo.getIsDurable().equals(1), subscribeHandler);
            } catch (Exception ex) {
                checkVo.setError(ex.getMessage());
            }
        }
        mqSubscribeMapper.updateSubscribe(checkVo);
        return null;
    }

}
