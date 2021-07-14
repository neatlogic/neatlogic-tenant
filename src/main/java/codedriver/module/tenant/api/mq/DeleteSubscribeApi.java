/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.mq;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.MQ_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.mq.SubscribeNotFoundException;
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
    @Description(desc = "删除消息队列订阅接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        SubscribeVo subVo = mqSubscribeMapper.getSubscribeById(jsonObj.getLong("id"));
        if (subVo == null) {
            throw new SubscribeNotFoundException(jsonObj.getLong("id"));
        }
        mqSubscribeMapper.deleteSubscribeById(jsonObj.getLong("id"));
        SubscribeManager.destroy(subVo.getTopicName(), subVo.getName());
        return null;
    }

}
