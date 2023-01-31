/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.tenant.api.test;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.annotation.ResubmitInterval;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.transaction.core.AfterTransactionJob;
import neatlogic.module.tenant.dao.mapper.TestMapper;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Deprecated
@Component
//@Transactional
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
    @Input({@Param(name = "content", type = ApiParamType.STRING, isRequired = true)})
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String content = jsonObj.getString("content");
        AfterTransactionJob<String> job = new AfterTransactionJob<>("TEST");
        job.execute(content, c -> {
            System.out.println(testMapper.getContent());
        });
        testMapper.insertContent(content);
        System.out.println("done");
        Thread.sleep(20000L);
        return null;
    }

    @Override
    public String getToken() {
        return "/testbg";
    }

}
