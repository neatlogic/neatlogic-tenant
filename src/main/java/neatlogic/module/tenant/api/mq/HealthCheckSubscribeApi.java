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
