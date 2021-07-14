/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.mq;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.MQ_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.mq.dao.mapper.MqSubscribeMapper;
import codedriver.framework.mq.dto.SubscribeVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = MQ_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetSubscribeApi extends PrivateApiComponentBase {

    @Resource
    private MqSubscribeMapper mqSubscribeMapper;

    @Override
    public String getToken() {
        return "/mq/subscribe/get";
    }

    @Override
    public String getName() {
        return "获取消息队列订阅信息";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "id", isRequired = true)})
    @Output({@Param(explode = SubscribeVo.class)})
    @Description(desc = "获取消息队列订阅信息接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        return mqSubscribeMapper.getSubscribeById(jsonObj.getLong("id"));
    }

}
