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

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.auth.label.NOTIFY_JOB_MODIFY;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.dto.module.ModuleGroupVo;
import neatlogic.framework.exception.type.PermissionDeniedException;
import neatlogic.framework.notify.core.NotifyPolicyHandlerFactory;
import neatlogic.framework.notify.dto.NotifyPolicyHandlerVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyPolicyHandlerListApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "notify/policy/handler/list";
    }

    @Override
    public String getName() {
        return "??????????????????????????????";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({
            @Param(explode = ValueTextVo[].class, desc = "????????????????????????")
    })
    @Description(desc = "??????????????????????????????")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Map<String, List<NotifyPolicyHandlerVo>> moduleGroupNotifyPolicyHandlerListMap = new HashMap<>();
        List<NotifyPolicyHandlerVo> list = new ArrayList<>(NotifyPolicyHandlerFactory.getNotifyPolicyHandlerList());
        for (NotifyPolicyHandlerVo notifyPolicyHandlerVo : list) {
            /* ???????????????????????????????????????????????????????????????????????????????????????????????????????????? */
            if (AuthActionChecker.check(notifyPolicyHandlerVo.getAuthName())) {
                moduleGroupNotifyPolicyHandlerListMap.computeIfAbsent(notifyPolicyHandlerVo.getModuleGroup(), key -> new ArrayList<>()).add(notifyPolicyHandlerVo);
            }
        }
        JSONArray resultTree = new JSONArray();
        List<ModuleGroupVo> moduleGroupList = TenantContext.get().getActiveModuleGroupList();
        for (ModuleGroupVo moduleGroupVo : moduleGroupList) {
            List<NotifyPolicyHandlerVo> notifyPolicyHandlerVoList = moduleGroupNotifyPolicyHandlerListMap.get(moduleGroupVo.getGroup());
            if (CollectionUtils.isNotEmpty(notifyPolicyHandlerVoList)) {
                JSONArray children = new JSONArray();
                for (NotifyPolicyHandlerVo notifyPolicyHandlerVo : notifyPolicyHandlerVoList) {
                    JSONObject child = new JSONObject();
                    child.put("value", notifyPolicyHandlerVo.getHandler());
                    child.put("text", notifyPolicyHandlerVo.getName());
                    child.put("isAllowMultiPolicy", notifyPolicyHandlerVo.getIsAllowMultiPolicy());
                    children.add(child);
                }
                JSONObject moduleGroupObj = new JSONObject();
                moduleGroupObj.put("value", moduleGroupVo.getGroup());
                moduleGroupObj.put("text", moduleGroupVo.getGroupName());
                moduleGroupObj.put("children", children);
                resultTree.add(moduleGroupObj);
            }
        }
        if (AuthActionChecker.check(NOTIFY_JOB_MODIFY.class)) {
            resultTree.add(new ValueTextVo("????????????", "????????????"));
        }
        if (CollectionUtils.isEmpty(resultTree)) {
            throw new PermissionDeniedException();
        }
        return resultTree;
    }

}
