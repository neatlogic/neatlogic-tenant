/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.notify;

import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.dto.NotifyTreeVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyPolicyHandlerGroupListApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "notify/policy/handlergroup/list";
    }

    @Override
    public String getName() {
        return "获取通知策略分类分组列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({
            @Param(explode = NotifyTreeVo[].class, desc = "通知策略分类列表")
    })
    @Description(desc = "获取通知策略分类分组列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        return NotifyPolicyHandlerFactory.getModuleTreeVoList();
    }

}
