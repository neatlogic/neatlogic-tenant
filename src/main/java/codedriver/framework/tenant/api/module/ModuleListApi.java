package codedriver.framework.tenant.api.module;

import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.dto.ModuleVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
public class ModuleListApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "module/list";
	}

	@Override
	public String getName() {
		return "模块列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Output({
		@Param(name = "Return", explode = ModuleVo[].class, desc = "模块列表")
	})
	@Description(desc = "模块列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		List<ModuleVo> moduleList = TenantContext.get().getActiveModuleList();
		return moduleList;
	}

}
