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

import java.util.ArrayList;
import java.util.List;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.ConditionParamVo;
import neatlogic.framework.notify.core.INotifyPolicyHandler;
import neatlogic.framework.notify.core.NotifyPolicyHandlerFactory;
import neatlogic.framework.notify.dao.mapper.NotifyMapper;
import neatlogic.framework.notify.dto.NotifyPolicyConfigVo;
import neatlogic.framework.notify.dto.NotifyPolicyVo;
import neatlogic.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import neatlogic.framework.notify.exception.NotifyPolicyNotFoundException;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyPolicyConditionOptionListApi extends PrivateApiComponentBase {

    @Autowired
    private NotifyMapper notifyMapper;

    @Override
    public String getToken() {
        return "notify/policy/conditionoption/list";
    }

    @Override
    public String getName() {
        return "通知策略条件选项列表接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "keyword", type = ApiParamType.STRING, desc = "模糊匹配"),
        @Param(name = "policyId", type = ApiParamType.LONG, isRequired = true, desc = "策略id")})
    @Output({@Param(name = "conditonOptionList", explode = ConditionParamVo[].class, desc = "条件选项列表")})
    @Description(desc = "通知策略条件选项列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<ConditionParamVo> conditonOptionList = new ArrayList<>();
        Long policyId = jsonObj.getLong("policyId");
        NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
        if (notifyPolicyVo == null) {
            throw new NotifyPolicyNotFoundException(policyId.toString());
        }
        INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyVo.getHandler());
        if (notifyPolicyHandler == null) {
            throw new NotifyPolicyHandlerNotFoundException(notifyPolicyVo.getHandler());
        }
        List<ConditionParamVo> systemConditionOptionList = notifyPolicyHandler.getSystemConditionOptionList();
        NotifyPolicyConfigVo config = notifyPolicyVo.getConfig();
        List<ConditionParamVo> customParamList = config.getParamList();
        systemConditionOptionList.addAll(customParamList);
        String keyword = jsonObj.getString("keyword");
        for (ConditionParamVo notifyPolicyParamVo : systemConditionOptionList) {
            if (StringUtils.isNotBlank(keyword)) {
                if (!notifyPolicyParamVo.getName().toLowerCase().contains(keyword.toLowerCase())
                    && !notifyPolicyParamVo.getLabel().toLowerCase().contains(keyword.toLowerCase())) {
                    continue;
                }
            }
            conditonOptionList.add(notifyPolicyParamVo);
        }
        conditonOptionList.sort((e1, e2) -> e1.getName().compareToIgnoreCase(e2.getName()));
        JSONObject resultObj = new JSONObject();
        resultObj.put("conditonOptionList", conditonOptionList);
        return resultObj;
    }

}
