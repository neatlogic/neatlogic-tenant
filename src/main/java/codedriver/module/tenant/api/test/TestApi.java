package codedriver.module.tenant.api.test;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.NO_AUTH;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.dao.mapper.TestMapper;

@Deprecated
@AuthAction(action = NO_AUTH.class)
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
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        return testMapper.testBg();
    }
    
    public static void main(String[] atgv) {
        String sql ="aasa BG{123}";
        sql = sql.replaceAll("BG\\{([^\\}]+)\\}", "new$1new");
        System.out.println(sql);
    }

    @Override
    public String getToken() {
        return "/testbg";
    }

}
