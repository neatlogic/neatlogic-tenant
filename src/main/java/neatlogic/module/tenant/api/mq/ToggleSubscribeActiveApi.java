/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

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
        return "更新消息队列订阅激活状态";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", isRequired = true, type = ApiParamType.LONG, desc = "id"),
            @Param(name = "isActive", isRequired = true, type = ApiParamType.INTEGER, desc = "是否激活")})
    @Description(desc = "更新消息队列订阅激活状态接口")
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
