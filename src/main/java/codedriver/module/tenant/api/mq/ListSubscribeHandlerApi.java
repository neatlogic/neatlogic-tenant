/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.mq;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.MQ_MODIFY;
import codedriver.framework.mq.core.SubscribeHandlerFactory;
import codedriver.framework.mq.dto.SubscribeHandlerVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = MQ_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ListSubscribeHandlerApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "/mq/subscribehandler/list";
    }

    @Override
    public String getName() {
        return "获取消息队列订阅处理器列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({@Param(explode = SubscribeHandlerVo[].class)})
    @Description(desc = "获取消息队列订阅处理器列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        return SubscribeHandlerFactory.getSubscribeHandlerList();
    }

}
