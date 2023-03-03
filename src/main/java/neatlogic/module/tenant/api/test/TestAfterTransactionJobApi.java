/*
Copyright(c) $today.year NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. 
 */

package neatlogic.module.tenant.api.test;

import neatlogic.framework.asynchronization.thread.NeatLogicThread;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadpool.CachedThreadPool;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.transaction.core.AfterTransactionJob;
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
        NeatLogicThread runnable = new NeatLogicThread("TEST-THREAD") {
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
