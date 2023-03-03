/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.healthcheck;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.dao.mapper.healthcheck.SqlStatusMapper;
import neatlogic.framework.dto.healthcheck.DataSourceInfoVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetInnoDbStatusApi extends PrivateApiComponentBase {
    @Resource
    private SqlStatusMapper sqlStatusMapper;

    @Override
    public String getToken() {
        return "/healthcheck/innodb/status";
    }

    @Override
    public String getName() {
        return "获取Innodb引擎状态";
    }

    @Override
    public String getConfig() {
        return null;
    }


    @Output({
            @Param(explode = DataSourceInfoVo.class),
    })
    @Description(desc = "获取Innodb引擎状态接口，用于查看死锁信息")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        return sqlStatusMapper.getInnodbStatus();
    }


}
