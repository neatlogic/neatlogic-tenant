/*
 * Copyright (C) 2025  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neatlogic.module.tenant.api.util;


import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.SubmitKeyManager;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class CleanSubmitKeyApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "/submit/key/clean";
    }

    @Override
    public String getName() {
        return "nmtau.cleansubmitkeyapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }


    @Input({
            @Param(name = "isAll", type = ApiParamType.STRING, desc = "1:清理所有 0:清理超时，默认清理超时")
    })
    @Output({})
    @Description(desc = "nmtau.cleansubmitkeyapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Integer isAll = paramObj.getInteger("isAll");
        if (isAll != null && isAll == 1) {
            SubmitKeyManager.clear();
        } else {
            SubmitKeyManager.cleanupExpiredKeys();
        }

        return null;
    }
}
