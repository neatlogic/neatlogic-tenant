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

import java.util.List;
import java.util.Objects;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.auth.label.NOTIFY_POLICY_MODIFY;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.notify.core.INotifyPolicyHandler;
import neatlogic.framework.notify.core.NotifyPolicyHandlerFactory;
import neatlogic.framework.notify.dao.mapper.NotifyMapper;
import neatlogic.framework.notify.dto.NotifyActionVo;
import neatlogic.framework.notify.dto.NotifyPolicyConfigVo;
import neatlogic.framework.notify.dto.NotifyPolicyVo;
import neatlogic.framework.notify.dto.NotifyTemplateVo;
import neatlogic.framework.notify.dto.NotifyTriggerNotifyVo;
import neatlogic.framework.notify.dto.NotifyTriggerVo;
import neatlogic.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import neatlogic.framework.notify.exception.NotifyPolicyNotFoundException;
import neatlogic.framework.notify.exception.NotifyTemplateReferencedCannotBeDeletedException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

@Service
@Transactional
@AuthAction(action = NOTIFY_POLICY_MODIFY.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class NotifyPolicyTemplateDeleteApi extends PrivateApiComponentBase {

    @Autowired
    private NotifyMapper notifyMapper;

    @Override
    public String getToken() {
        return "notify/policy/template/delete";
    }

    @Override
    public String getName() {
        return "通知模板删除接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "policyId", type = ApiParamType.LONG, isRequired = true, desc = "策略id"),
            @Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "模板id")})
    @Description(desc = "通知模板删除接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long policyId = jsonObj.getLong("policyId");
        NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
        if (notifyPolicyVo == null) {
            throw new NotifyPolicyNotFoundException(policyId.toString());
        }
        INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyVo.getHandler());
        if (notifyPolicyHandler == null) {
            throw new NotifyPolicyHandlerNotFoundException(notifyPolicyVo.getHandler());
        }

        Long id = jsonObj.getLong("id");
        int index = -1;
        String notifyTemplateName = null;
        NotifyPolicyConfigVo config = notifyPolicyVo.getConfig();
        List<NotifyTemplateVo> templateList = config.getTemplateList();
        for (int i = 0; i < templateList.size(); i++) {
            NotifyTemplateVo notifyTemplateVo = templateList.get(i);
            if (id.equals(notifyTemplateVo.getId())) {
                index = i;
                notifyTemplateName = notifyTemplateVo.getName();
            }
        }
        if (index == -1) {
            return null;
        }
        // 判断模板是否被引用
        List<NotifyTriggerVo> triggerList = config.getTriggerList();
        for (NotifyTriggerVo notifyTriggerVo : triggerList) {
            for (NotifyTriggerNotifyVo notifyTriggerNotifyVo : notifyTriggerVo.getNotifyList()) {
                for (NotifyActionVo notifyActionVo : notifyTriggerNotifyVo.getActionList()) {
                    if (Objects.equals(notifyActionVo.getTemplateId(), id)) {
                        throw new NotifyTemplateReferencedCannotBeDeletedException(notifyTemplateName);
                    }
                }
            }
        }
        templateList.remove(index);
        notifyMapper.updateNotifyPolicyById(notifyPolicyVo);
        return null;
    }

}
