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

package neatlogic.module.tenant.api.notify;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dependency.constvalue.FrameworkFromType;
import neatlogic.framework.dependency.core.DependencyManager;
import neatlogic.framework.notify.dao.mapper.NotifyMapper;
import neatlogic.framework.notify.dto.NotifyPolicyVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyPolicySearchApi extends PrivateApiComponentBase {

    @Autowired
    private NotifyMapper notifyMapper;

    @Override
    public String getToken() {
        return "notify/policy/search";
    }

    @Override
    public String getName() {
        return "通知策略管理列表搜索接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字搜索"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否分页"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条数"),
            @Param(name = "handler", type = ApiParamType.STRING, isRequired = true, desc = "通知策略处理器")
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "tbodyList", explode = NotifyPolicyVo[].class, desc = "通知策略列表")
    })
    @Description(desc = "通知策略管理列表搜索接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        NotifyPolicyVo notifyPolicyVo = JSON.toJavaObject(jsonObj, NotifyPolicyVo.class);
        List<NotifyPolicyVo> tbodyList = notifyMapper.getNotifyPolicyList(notifyPolicyVo);

        for (NotifyPolicyVo notifyPolicy : tbodyList) {
            int count = DependencyManager.getDependencyCount(FrameworkFromType.NOTIFY_POLICY, notifyPolicy.getId());
            notifyPolicy.setReferenceCount(count);
        }
        resultObj.put("tbodyList", tbodyList);
        if (notifyPolicyVo.getNeedPage()) {
            int rowNum = notifyMapper.getNotifyPolicyCount(notifyPolicyVo);
            resultObj.put("currentPage", notifyPolicyVo.getCurrentPage());
            resultObj.put("pageSize", notifyPolicyVo.getPageSize());
            resultObj.put("pageCount", PageUtil.getPageCount(rowNum, notifyPolicyVo.getPageSize()));
            resultObj.put("rowNum", rowNum);
        }
        return resultObj;
    }

}
