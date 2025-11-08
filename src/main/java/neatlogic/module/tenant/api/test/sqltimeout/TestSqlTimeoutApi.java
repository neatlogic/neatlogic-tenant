/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.api.test.sqltimeout;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.dao.mapper.TestMapper;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

//@Component
@Deprecated
@Transactional
public class TestSqlTimeoutApi extends PrivateApiComponentBase {

    @Resource
    private TestMapper testMapper;

    @Override
    public String getName() {
        return "Test transaction timeout";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id1", type = ApiParamType.LONG, isRequired = true, desc = "ID1"),
            @Param(name = "id2", type = ApiParamType.LONG, isRequired = true, desc = "ID2"),
            @Param(name = "sleep", type = ApiParamType.LONG, desc = "睡眠时间，单位秒")
    })
    @Output({})
    @Description(desc = "Test transaction timeout")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        System.out.println("startTime=" + System.currentTimeMillis());
//        TransactionStatus transactionStatus = TransactionUtil.openTx();
//        try {
            Long id1 = paramObj.getLong("id1");
            Long id2 = paramObj.getLong("id2");
            testMapper.getProcessTaskByIdForUpdate(id1);
            System.out.println("id1=" + id1);
            Long sleep = paramObj.getLong("sleep");
            if (sleep != null) {
                TimeUnit.SECONDS.sleep(sleep);
            }
            testMapper.getProcessTaskByIdForUpdate(id2);
            System.out.println("id2=" + id2);
            System.out.println("endTime=" + System.currentTimeMillis());
//            TransactionUtil.commitTx(transactionStatus);
//        } catch (Exception e) {
//            TransactionUtil.rollbackTx(transactionStatus);
//        }
        return null;
    }

    @Override
    public String getToken() {
        return "test/sql/timeout";
    }
}
