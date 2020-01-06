package codedriver.module.tenant.api.test;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.core.ApiComponentBase;


@Service
public class esSaveApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "test/es/save";
	}

	@Override
	public String getName() {
		return "测试es保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}


	@Description(desc = "测试es保存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		/*String title = jsonObj.getString("title");
		List<String> tags = jsonObj.getJSONArray("tags").toJavaList(String.class);
		String userId = jsonObj.getString("user_id");
		int status = jsonObj.getIntValue("status");
		Config.pool.checkout(TenantContext.get().getTenantUuid());
		MultiAttrsObjectPatch patch = Config.pool.save(UUID.randomUUID().toString().replace("-", ""), 1);
		patch.set("title", title+TenantContext.get().getTenantUuid())
        .set("create_time", new Date(), false)
        .setStrings("tags", tags)
        .set("user_id", userId)
        .set("status", status)
        .commit();*/
		return 1;
	}

}
