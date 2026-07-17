/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.api.notify;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
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
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyPolicyGetApi extends PrivateApiComponentBase {

    @Resource
    private NotifyMapper notifyMapper;

    @Resource
    private NotifyPolicyService notifyPolicyService;

    @Override
    public String getToken() {
        return "notify/policy/get";
    }

    @Override
    public String getName() {
        return "nmtan.notifypolicygetapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    public boolean disableReturnCircularReferenceDetect() {
        return true;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "common.id")})
    @Output({@Param(explode = NotifyPolicyVo.class, desc = "common.tbodylist")})
    @Description(desc = "nmtan.notifypolicygetapi.getname")
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
        /** 根据通知处理器所属模块加载动态通知对象，ITSM仍对应process，RDM对应rdm。 */
        String notifyModuleGroup = NotifyPolicyHandlerFactory.getModuleGroupIdByHandler(notifyPolicyVo.getHandler());
        Map<String, UserTypeVo> userTypeVoMap = UserTypeFactory.getUserTypeMap();
        UserTypeVo userTypeVo = userTypeVoMap.get(notifyModuleGroup);
        final Map<String, String> moduleUserType = new HashMap<>();
        if (userTypeVo != null && userTypeVo.getValues() != null) {
            moduleUserType.putAll(userTypeVo.getValues());
        }
        NotifyPolicyConfigVo config = notifyPolicyVo.getConfig();
        List<NotifyTriggerVo> triggerList = config.getTriggerList();
        List<NotifyTriggerVo> notifyTriggerList = notifyPolicyHandler.getNotifyTriggerList();
        if (CollectionUtils.isNotEmpty(notifyTriggerList)) {
            /* 矫正旧配置数据中的触发点 */
            /* 多删 -- 删除已经不存在的触发点 */
            triggerList.removeIf(next -> notifyTriggerList.stream().noneMatch(o -> o.getTrigger().equals(next.getTrigger())));
            List<NotifyTriggerVo> triggerArray = new ArrayList<>();
            for (NotifyTriggerVo notifyTrigger : notifyTriggerList) {
                boolean existed = false;
                for (NotifyTriggerVo triggerObj : triggerList) {
                    if (Objects.equals(notifyTrigger.getTrigger(), triggerObj.getTrigger())) {
                        /* 补充通知对象详细信息 */
                        notifyPolicyService.addReceiverExtraInfo(moduleUserType, triggerObj);
                        triggerObj.setTriggerName(notifyTrigger.getTriggerName());
                        triggerObj.setDescription(notifyTrigger.getDescription());
                        triggerArray.add(triggerObj);
                        existed = true;
                        break;
                    }
                }
                /* 少补 -- 新增老数据中没有而现在有的触发点 */
                if (!existed) {
                    triggerArray.add(notifyTrigger);
                }
            }
            config.setTriggerList(triggerArray);
        }
        List<ConditionParamVo> systemParamList = notifyPolicyHandler.getSystemParamList();
        List<ConditionParamVo> systemConditionOptionList = notifyPolicyHandler.getSystemConditionOptionList();
        List<ConditionParamVo> paramList = config.getParamList();
        if (CollectionUtils.isNotEmpty(paramList)) {
            if (CollectionUtils.isEmpty(systemConditionOptionList)) {
                systemConditionOptionList = new ArrayList<>();
            }
            systemConditionOptionList.addAll(paramList);
            systemConditionOptionList.sort((e1, e2) -> e1.getName().compareToIgnoreCase(e2.getName()));
            if (CollectionUtils.isEmpty(systemParamList)) {
                systemParamList = new ArrayList<>();
            }
            for (ConditionParamVo conditionParamVo : paramList) {
                systemParamList.add(conditionParamVo.clone());
            }
            systemParamList.sort((e1, e2) -> e1.getName().compareToIgnoreCase(e2.getName()));
        }
        config.setConditionOptionList(systemConditionOptionList);
        config.setParamList(systemParamList);
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
