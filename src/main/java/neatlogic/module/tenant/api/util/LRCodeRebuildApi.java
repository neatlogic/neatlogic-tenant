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

package neatlogic.module.tenant.api.util;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.lrcode.LRCodeManager;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class LRCodeRebuildApi extends PrivateApiComponentBase {


    @Override
    public String getToken() {
        return "/util/lrcoderebuild";
    }

    @Override
    public String getName() {
        return "重建左右编码";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "tableName", type = ApiParamType.STRING, isRequired = true, desc = "表名"),
            @Param(name = "idKey", type = ApiParamType.STRING, isRequired = true, desc = "id字段名"),
            @Param(name = "parentIdKey", type = ApiParamType.STRING, isRequired = true, desc = "父id字段名"),
            @Param(name = "sortKey", type = ApiParamType.STRING, desc = "排序字段名"),
            @Param(name = "condition", type = ApiParamType.STRING, desc = "条件")
    })
    @Description(desc = "重建左右编码接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String tableName = jsonObj.getString("tableName");
        String idKey = jsonObj.getString("idKey");
        String parentIdKey = jsonObj.getString("parentIdKey");
        String sortKey = jsonObj.getString("sortKey");
        String condition = jsonObj.getString("condition");
        LRCodeManager.rebuildLeftRightCodeOrderBySortKey(tableName, idKey, parentIdKey, condition, sortKey);
        return null;
    }

}
