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

import com.alibaba.fastjson.JSONObject;
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
import neatlogic.framework.notify.dto.NotifyPolicyVo;
import neatlogic.framework.notify.dto.NotifyTriggerVo;
import neatlogic.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import neatlogic.framework.notify.exception.NotifyPolicyNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.usertype.UserTypeFactory;
import neatlogic.framework.util.$;
import neatlogic.module.tenant.service.notify.NotifyPolicyService;
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
        return "通知策略信息获取接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "策略id")})
    @Output({@Param(explode = NotifyPolicyVo.class, desc = "策略信息")})
    @Description(desc = "通知策略信息获取接口")
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
        /** 获取工单干系人枚举 */
        //TODO 没有兼容多模块
        Map<String, UserTypeVo> userTypeVoMap = UserTypeFactory.getUserTypeMap();
        UserTypeVo UsertypeVo = userTypeVoMap.get("process");
        Map<String, String> processUserType = UsertypeVo.getValues();

        NotifyPolicyConfigVo config = notifyPolicyVo.getConfig();
        List<NotifyTriggerVo> triggerList = config.getTriggerList();
        List<NotifyTriggerVo> notifyTriggerList = notifyPolicyHandler.getNotifyTriggerList();
        if (CollectionUtils.isNotEmpty(notifyTriggerList)) {
            /** 矫正旧配置数据中的触发点 */
            /** 多删 -- 删除已经不存在的触发点 */
            Iterator<NotifyTriggerVo> iterator = triggerList.iterator();
            while (iterator.hasNext()) {
                NotifyTriggerVo next = iterator.next();
                if (!notifyTriggerList.stream().anyMatch(o -> o.getTrigger().equals(next.getTrigger()))) {
                    iterator.remove();
                }
            }
            List<NotifyTriggerVo> triggerArray = new ArrayList<>();
            for (NotifyTriggerVo notifyTrigger : notifyTriggerList) {
                boolean existed = false;
                for (NotifyTriggerVo triggerObj : triggerList) {
                    if (Objects.equals(notifyTrigger.getTrigger(), triggerObj.getTrigger())) {
                        /** 补充通知对象详细信息 */
                        notifyPolicyService.addReceiverExtraInfo(processUserType, triggerObj);
                        triggerObj.setTriggerName(notifyTrigger.getTriggerName());
                        triggerObj.setDescription(notifyTrigger.getDescription());
                        triggerArray.add(triggerObj);
                        existed = true;
                        break;
                    }
                }
                /** 少补 -- 新增老数据中没有而现在有的触发点 */
                if (!existed) {
                    triggerArray.add(notifyTrigger);
                }
            }
            config.setTriggerList(triggerArray);
        }
        List<ConditionParamVo> systemParamList = notifyPolicyHandler.getSystemParamList();
        List<ConditionParamVo> systemConditionOptionList = notifyPolicyHandler.getSystemConditionOptionList();
        if (CollectionUtils.isNotEmpty(systemConditionOptionList)) {
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
        }
        int count = DependencyManager.getDependencyCount(FrameworkFromType.NOTIFY_POLICY, id);
        notifyPolicyVo.setReferenceCount(count);

        String moduleGroup = NotifyPolicyHandlerFactory.getModuleGroupIdByHandler(notifyPolicyVo.getHandler());
        if (moduleGroup == null) {
            throw new NotifyPolicyHandlerNotFoundException(notifyPolicyVo.getHandler());
        }
        String moduleGroupName = "";
        ModuleGroupVo moduleGroupVo = ModuleUtil.getModuleGroup(moduleGroup);
        if (moduleGroupVo != null) {
            moduleGroupName = moduleGroupVo.getGroupName();
        }
        String handlerName = $.t(notifyPolicyHandler.getName());
        notifyPolicyVo.setPath(moduleGroupName + "/" + handlerName + "/" + notifyPolicyVo.getName());
        return notifyPolicyVo;
    }

}
