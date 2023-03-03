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
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.healthcheck.DatabaseFragmentVo;
import neatlogic.framework.healthcheck.dao.mapper.DatabaseFragmentMapper;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class RebuildTableApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "/healthcheck/table/rebuild";
    }

    @Override
    public String getName() {
        return "重建表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Resource
    private DatabaseFragmentMapper databaseFragmentMapper;


    @Input({@Param(name = "tableName", type = ApiParamType.STRING, desc = "表名", isRequired = true), @Param(name = "schemaType", type = ApiParamType.ENUM, rule = "main,data", desc = "库类型", isRequired = true)})
    @Description(desc = "重建表接口，一般空闲空间太大的表才需要进行此操作")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        DatabaseFragmentVo databaseFragmentVo = JSONObject.toJavaObject(paramObj, DatabaseFragmentVo.class);
        databaseFragmentMapper.rebuildTable(databaseFragmentVo.getSchema(), paramObj.getString("tableName"));
        return null;
    }


}
