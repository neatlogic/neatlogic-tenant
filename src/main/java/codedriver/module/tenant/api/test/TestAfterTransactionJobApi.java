/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.test;

import codedriver.framework.asynchronization.thread.CodeDriverThread;
import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.asynchronization.threadpool.CachedThreadPool;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.transaction.core.AfterTransactionJob;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

//@Transactional
@Service
public class TestAfterTransactionJobApi extends PrivateApiComponentBase {
    @Resource
    UserMapper userMapper;

    @Override
    public String getName() {
        return "测试事务提交后线程获取租户";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Description(desc = "测试事务提交后线程获取租户")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        AfterTransactionJob<String> job = new AfterTransactionJob<>("TEST-AFTER_TRANSACTION_JOB");
       /* job.execute(TenantContext.get().getTenantUuid(), data -> {

            //System.out.println("Param: "+data);
            try {
                Thread.sleep(Math.round(Math.random() * (30000 - 1)));
                TenantContext.get().getTenantUuid();
            }catch (Exception ex){
                ex.printStackTrace();
            }
            //System.out.println("Context: "+TenantContext.get().getTenantUuid());
            //userMapper.getUserByUuid(UserContext.get().getUserUuid());
        });*/
        /*BatchRunner<ModuleVo> runner = new BatchRunner<>();
        runner.execute(TenantContext.get().getActiveModuleList(), 20, data -> {
            try {
                Thread.sleep(Math.round(Math.random() * (30000 - 1)));
                TenantContext.get().getTenantUuid();
            } catch (Exception e) {
                e.printStackTrace();
            }
        },"TEST-BATCH-THREAD");*/
        try {
            TenantContext.get().getTenantUuid();
        }catch (Exception  ex){
            ex.printStackTrace();
        }
        CodeDriverThread runnable = new CodeDriverThread("TEST-THREAD") {
            @Override
            protected void execute() {
                try {
                    Thread.sleep(Math.round(Math.random() * (10000 - 1)));
                    TenantContext.get().getTenantUuid();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        CachedThreadPool.execute(runnable);
        try {
            System.out.println(TenantContext.get().getTenantUuid());
        }catch (Exception  ex){
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public String getToken() {
        return "/test/afterTransactionJob";
    }

}
