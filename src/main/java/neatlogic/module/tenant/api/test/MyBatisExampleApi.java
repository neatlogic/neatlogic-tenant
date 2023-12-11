package neatlogic.module.tenant.api.test;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.dao.mapper.TestMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class MyBatisExampleApi extends PrivateApiComponentBase {

    @Resource
    TestMapper testMapper;

    @Override
    public String getName() {
        return "mybatis test";
    }
    @Input({})
    @Output({})
    @Description(desc = "mybatis test")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
//        Map<String, Object> map = testMapper.selectTColumn();
//        System.out.println("map = " + map);
//        for (Map.Entry<String, Object> entry : map.entrySet()) {
//            System.out.println("key = " + entry.getKey() + " -> " + entry.getValue().getClass().getName());
//        }
        return null;
    }

    @Override
    public String getToken() {
        return "mybatis/test";
    }
}
