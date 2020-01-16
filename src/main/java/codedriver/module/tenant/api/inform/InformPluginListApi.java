package codedriver.module.tenant.api.inform;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.inform.core.InformComponentFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
public class InformPluginListApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "inform/plugin/list";
	}

	@Override
	public String getName() {
		return "通知插件列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Description(desc = "通知插件列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return InformComponentFactory.getInformPluginTypeList();
	}

}
