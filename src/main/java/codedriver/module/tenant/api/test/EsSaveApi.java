package codedriver.module.tenant.api.test;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.core.ApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class EsSaveApi extends ApiComponentBase {

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

    @Autowired
    private ObjectPoolService objectPoolService;


    @Description(desc = "测试es保存对象接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String id = jsonObj.getString("id");
        if (id == null || id.isEmpty()) {
            return "";
        }
        final String tagsKey = "tags";
        List<String> tags = Util.getList(jsonObj, tagsKey);
        if (tags.isEmpty()) {
            jsonObj.remove(tagsKey);
        } else {
            jsonObj.put(tagsKey, tags);
        }
        objectPoolService.saveTask(TenantContext.get().getTenantUuid(), id, jsonObj);
        return id;
    }

}