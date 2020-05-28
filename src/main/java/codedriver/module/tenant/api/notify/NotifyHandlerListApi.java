package codedriver.module.tenant.api.notify;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.notify.core.NotifyHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@IsActived
public class NotifyHandlerListApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "notify/handler/list/new";
	}

	@Override
	public String getName() {
		return "通知插件列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Output({
		@Param(name = "notifyHandlerList", explode = ValueTextVo[].class, desc = "通知插件列表")
	})
	@Description(desc = "通知插件列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject resultObj = new JSONObject();
		resultObj.put("notifyHandlerList", NotifyHandlerFactory.getNotifyHandlerTypeList());
		return resultObj;
	}

}
