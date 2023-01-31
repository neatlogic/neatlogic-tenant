/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.tenant.api.notify;

import neatlogic.framework.notify.core.NotifyPolicyHandlerFactory;
import neatlogic.framework.notify.dto.NotifyTreeVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
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
