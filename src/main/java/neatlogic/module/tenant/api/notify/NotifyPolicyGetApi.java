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
import neatlogic.framework.common.util.ModuleUtil;
import neatlogic.framework.dependency.constvalue.FrameworkFromType;
import neatlogic.framework.dependency.core.DependencyManager;
import neatlogic.framework.dto.ConditionParamVo;
import neatlogic.framework.dto.UserTypeVo;
import neatlogic.framework.dto.module.ModuleGroupVo;
import neatlogic.framework.notify.core.INotifyPolicyHandler;
import neatlogic.framework.notify.core.NotifyPolicyHandlerFactory;
import neatlogic.framework.notify.dao.mapper.NotifyMapper;
import neatlogic.framework.notify.dto.NotifyPolicyConfigVo;
import neatlogic.framework.notify.dto.NotifyPolicyHandlerVo;
import neatlogic.framework.notify.dto.NotifyPolicyVo;
import neatlogic.framework.notify.dto.NotifyTriggerVo;
import neatlogic.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import neatlogic.framework.notify.exception.NotifyPolicyNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.usertype.UserTypeFactory;
import neatlogic.module.tenant.service.notify.NotifyPolicyService;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyPolicyGetApi extends PrivateApiComponentBase {

    @Autowired
    private NotifyMapper notifyMapper;

    @Autowired
    private NotifyPolicyService notifyPolicyService;

    @Override
    public String getToken() {
        return "notify/policy/get";
    }

    @Override
    public String getName() {
        return "??????????????????????????????";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "??????id")})
    @Output({@Param(explode = NotifyPolicyVo.class, desc = "????????????")})
    @Description(desc = "??????????????????????????????")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(id);
        if (notifyPolicyVo == null) {
            throw new NotifyPolicyNotFoundException(id.toString());
        }
        INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyVo.getHandler());
        if (notifyPolicyHandler == null) {
            throw new NotifyPolicyHandlerNotFoundException(notifyPolicyVo.getHandler());
        }
        /** ??????????????????????????? */
        //TODO ?????????????????????
        Map<String, UserTypeVo> userTypeVoMap = UserTypeFactory.getUserTypeMap();
        UserTypeVo UsertypeVo = userTypeVoMap.get("process");
        Map<String, String> processUserType = UsertypeVo.getValues();

        NotifyPolicyConfigVo config = notifyPolicyVo.getConfig();
        List<NotifyTriggerVo> triggerList = config.getTriggerList();
        List<NotifyTriggerVo> notifyTriggerList = notifyPolicyHandler.getNotifyTriggerList();
        /** ???????????????????????????????????? */
        /** ?????? -- ????????????????????????????????? */
        Iterator<NotifyTriggerVo> iterator = triggerList.iterator();
        while (iterator.hasNext()){
            NotifyTriggerVo next = iterator.next();
            if(!notifyTriggerList.stream().anyMatch(o -> o.getTrigger().equals(next.getTrigger()))){
                iterator.remove();
            }
        }
        List<NotifyTriggerVo> triggerArray = new ArrayList<>();
        for (NotifyTriggerVo notifyTrigger : notifyTriggerList) {
            boolean existed = false;
            for (NotifyTriggerVo triggerObj : triggerList) {
                if (Objects.equals(notifyTrigger.getTrigger(), triggerObj.getTrigger())) {
                    /** ?????????????????????????????? */
                    notifyPolicyService.addReceiverExtraInfo(processUserType, triggerObj);
                    triggerObj.setDescription(notifyTrigger.getDescription());
                    triggerArray.add(triggerObj);
                    existed = true;
                    break;
                }
            }
            /** ?????? -- ???????????????????????????????????????????????? */
            if (!existed) {
                triggerArray.add(notifyTrigger);
            }
        }
        config.setTriggerList(triggerArray);
        List<ConditionParamVo> systemParamList = notifyPolicyHandler.getSystemParamList();
        List<ConditionParamVo> systemConditionOptionList = notifyPolicyHandler.getSystemConditionOptionList();
        List<ConditionParamVo> paramList = config.getParamList();
        if (CollectionUtils.isNotEmpty(paramList)) {
            for (ConditionParamVo param : paramList) {
                systemParamList.add(param);
                systemConditionOptionList.add(new ConditionParamVo(param));
            }
        }

        systemParamList.sort((e1, e2) -> e1.getName().compareToIgnoreCase(e2.getName()));
        systemConditionOptionList.sort((e1, e2) -> e1.getName().compareToIgnoreCase(e2.getName()));
        config.setParamList(systemParamList);
        config.setConditionOptionList(systemConditionOptionList);

        int count = DependencyManager.getDependencyCount(FrameworkFromType.NOTIFY_POLICY, id);
        notifyPolicyVo.setReferenceCount(count);

        String moduleGroupName = "";
        List<NotifyPolicyHandlerVo> notifyPolicyHandlerList = NotifyPolicyHandlerFactory.getNotifyPolicyHandlerList();
        for (NotifyPolicyHandlerVo notifyPolicyHandlerVo : notifyPolicyHandlerList) {
            if (Objects.equals(notifyPolicyHandlerVo.getHandler(), notifyPolicyVo.getHandler())) {
                ModuleGroupVo moduleGroupVo = ModuleUtil.getModuleGroup(notifyPolicyHandlerVo.getModuleGroup());
                if (moduleGroupVo != null) {
                    moduleGroupName = moduleGroupVo.getGroupName();
                }
            }
        }
        String handlerName = notifyPolicyHandler.getName();
        notifyPolicyVo.setPath(moduleGroupName + "/" + handlerName + "/" + notifyPolicyVo.getName());
        return notifyPolicyVo;
    }

}
