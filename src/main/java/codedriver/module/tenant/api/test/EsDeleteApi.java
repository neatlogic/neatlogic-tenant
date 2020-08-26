package codedriver.module.tenant.api.test;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

public class EsDeleteApi extends PrivateApiComponentBase {
    @Autowired
    private ObjectPoolService objectPoolService;

    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String id = jsonObj.getString("id");
        if (id == null || id.isEmpty()) {
            return "";
        }

        objectPoolService.deleteTask(TenantContext.get().getTenantUuid(), id);
        return id;
    }

    @Override
    public String getToken() {
        return "test/es/delete";
    }

    @Override
    public String getName() {
        return "测试ES删除对象接口";
    }

    @Override
    public String getConfig() {
        return null;
    }
}
