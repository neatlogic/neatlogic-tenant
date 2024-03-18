/*Copyright (C) 2023  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tenant.api.test;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.thread.NeatLogicThread;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadpool.CachedThreadPool;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.transaction.core.AfterTransactionJob;

import javax.annotation.Resource;

//@Transactional
//@Service
@Deprecated
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
