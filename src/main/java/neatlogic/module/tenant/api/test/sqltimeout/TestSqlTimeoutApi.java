/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

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
