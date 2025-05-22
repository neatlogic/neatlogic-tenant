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
import neatlogic.framework.util.TimeUtil;
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
        return "nmtau.getsubmitkeyinfoapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }


    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页,默认第一页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目，默认1000条"),
    })
    @Output({})
    @Description(desc = "nmtau.getsubmitkeyinfoapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String keyword = paramObj.getString("keyword");
        Integer page = paramObj.getInteger("currentPage");
        if (page == null) {
            page = 1;
        }
        Integer pageSize = paramObj.getInteger("pageSize");
        if (pageSize == null) {
            pageSize = 1000;
        }
        long now = System.currentTimeMillis();
        List<Map<String, Object>> allItems = new ArrayList<>();

        for (Map.Entry<String, Long> entry : SubmitKeyManager.getAll().entrySet()) {
            if (StringUtils.isNotBlank(keyword) && !entry.getKey().contains(keyword)) {
                continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("key", entry.getKey());
            item.put("expireTime", TimeUtil.convertDateToString(new Date(entry.getValue()), TimeUtil.YYYY_MM_DD_HH_MM_SS));
            item.put("remainingSeconds", Math.max(0, (entry.getValue() - now) / 1000));
            allItems.add(item);
        }

        // 分页处理
        int total = allItems.size();
        int fromIndex = Math.min((page - 1) * pageSize, total);
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<Map<String, Object>> pageList = allItems.subList(fromIndex, toIndex);

        // 返回分页结果（你也可以封装成 PageResult 对象）
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("list", pageList);
        return result;
    }
}
