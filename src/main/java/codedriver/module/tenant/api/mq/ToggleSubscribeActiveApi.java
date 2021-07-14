/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.mq;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.MQ_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.mq.SubscribeHandlerNotFoundException;
import codedriver.framework.exception.mq.SubscribeNotFoundException;
import codedriver.framework.mq.core.ISubscribeHandler;
import codedriver.framework.mq.core.SubscribeHandlerFactory;
import codedriver.framework.mq.core.SubscribeManager;
import codedriver.framework.mq.dao.mapper.MqSubscribeMapper;
import codedriver.framework.mq.dto.SubscribeVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
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
