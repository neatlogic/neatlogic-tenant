package codedriver.module.tenant.api.notify;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyPolicyHandlerListApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		return "notify/policy/handler/list";
	}

	@Override
	public String getName() {
		return "通知策略分类列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Output({
		@Param(explode = ValueTextVo[].class, desc = "通知策略分类列表")
	})
	@Description(desc = "通知策略分类列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return NotifyPolicyHandlerFactory.getNotifyPolicyHandlerList();
	}

}
