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

package neatlogic.module.tenant.api.notify;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
        return "nmtan.notifypolicysearchapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "common.needpage"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize"),
            @Param(name = "handler", type = ApiParamType.STRING, isRequired = true, desc = "term.notify.handler")
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "tbodyList", explode = NotifyPolicyVo[].class, desc = "term.notify.policylist")
    })
    @Description(desc = "nmtan.notifypolicysearchapi.getname")
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
