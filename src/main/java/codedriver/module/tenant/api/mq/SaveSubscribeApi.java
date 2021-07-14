/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.mq;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.MQ_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.mq.SubscribeHandlerNotFoundException;
import codedriver.framework.exception.mq.SubscribeNameIsExistsException;
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
