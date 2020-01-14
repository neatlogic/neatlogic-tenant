package codedriver.module.tenant.api.test;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.restful.core.ApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EsUpdateApi extends ApiComponentBase {
    @Autowired
    private ObjectPoolService objectPoolService;

    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String id = jsonObj.getString("id");
        if (id == null) {
            return null;
        }
        String title = jsonObj.getString("title");
        if (title == null || title.isEmpty()) {
            return null;
        }

        objectPoolService.updateTaskTitle(TenantContext.get().getTenantUuid(), id, title);
        return id;
    }

    @Override
    public String getToken() {
        return "test/es/update";
    }

    @Override
    public String getName() {
        return "测试ES更新接口";
    }

    @Override
    public String getConfig() {
        return null;
    }
}
