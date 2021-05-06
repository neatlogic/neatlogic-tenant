package codedriver.module.tenant.api.test;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.FRAMEWORK_BASE;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.ResubmitInterval;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.dao.mapper.TestMapper;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Deprecated
@AuthAction(action = FRAMEWORK_BASE.class)
@Component
public class TestApi extends PrivateApiComponentBase {

    @Autowired
    private TestMapper testMapper;

    @Override
    public String getName() {
        return "测试BG";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Description(desc = "测试BG")
    @ResubmitInterval(value = 10)
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String str = jsonObj.getString("content");
        return testMapper.getContent(str);
    }

    @Override
    public String getToken() {
        return "/testbg";
    }

}
