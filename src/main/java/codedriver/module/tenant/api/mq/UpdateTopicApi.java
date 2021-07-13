/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.mq;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.MQ_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.mq.dao.mapper.MqTopicMapper;
import codedriver.framework.mq.dto.TopicVo;
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
public class UpdateTopicApi extends PrivateApiComponentBase {

    @Resource
    private MqTopicMapper mqTopicMapper;

    @Override
    public String getToken() {
        return "/mq/topic/update";
    }

    @Override
    public String getName() {
        return "更新消息队列主题激活状态";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "name", isRequired = true, type = ApiParamType.STRING, desc = "唯一标识"),
            @Param(name = "isActive", isRequired = true, type = ApiParamType.INTEGER, desc = "是否激活")})
    @Description(desc = "更新消息队列主题激活状态接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        TopicVo topicVo = JSONObject.toJavaObject(jsonObj, TopicVo.class);
        if (topicVo.getIsActive().equals(1)) {
            mqTopicMapper.deleteInActiveTopic(topicVo.getName());
        } else {
            mqTopicMapper.insertInActiveTopic(topicVo);
        }
        return null;
    }

}
