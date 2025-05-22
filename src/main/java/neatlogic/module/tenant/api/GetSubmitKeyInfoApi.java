/*
 * Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.
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

package neatlogic.module.tenant.api;


import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.SubmitKeyManager;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetSubmitKeyInfoApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "/submit/key/info/get";
    }

    @Override
    public String getName() {
        return "获取重复提交SubmitKeyMap";
    }

    @Override
    public String getConfig() {
        return null;
    }


    @Input({@Param(name = "keyword", type = ApiParamType.STRING, desc = "不填")})
    @Output({})
    @Description(desc = "获取重复提交SubmitKeyMap")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String keyword = paramObj.getString("keyword");
        long now = System.currentTimeMillis();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map.Entry<String, Long> entry : SubmitKeyManager.getAll().entrySet()) {
            if(StringUtils.isNotBlank(keyword) && !entry.getKey().contains(keyword)){
                    continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("key", entry.getKey());
            item.put("expireTime", new Date(entry.getValue()));
            item.put("remainingSeconds", Math.max(0, (entry.getValue() - now) / 1000));
            result.add(item);
        }

        return result;
    }
}
