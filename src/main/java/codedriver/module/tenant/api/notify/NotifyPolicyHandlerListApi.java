/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.notify;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.auth.label.NOTIFY_JOB_MODIFY;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.dto.ModuleGroupVo;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.dto.NotifyPolicyHandlerVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
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
        return "通知策略分类列表接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({
            @Param(explode = ValueTextVo[].class, desc = "通知策略分类列表")
    })
    @Description(desc = "通知策略分类列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Map<String, List<NotifyPolicyHandlerVo>> moduleGroupNotifyPolicyHandlerListMap = new HashMap<>();
        List<NotifyPolicyHandlerVo> list = new ArrayList<>(NotifyPolicyHandlerFactory.getNotifyPolicyHandlerList());
        for (NotifyPolicyHandlerVo notifyPolicyHandlerVo : list) {
            /* 通知策略与权限绑定，例如没有流程管理权限则无法编辑流程及流程步骤通知策略 */
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
            resultTree.add(new ValueTextVo("定时任务", "定时任务"));
        }
        if (CollectionUtils.isEmpty(resultTree)) {
            throw new PermissionDeniedException();
        }
        return resultTree;
    }

}
