package codedriver.module.tenant.api.reminder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import codedriver.framework.restful.core.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.ModuleVo;
import codedriver.framework.reminder.core.GlobalReminderHandlerFactory;
import codedriver.framework.reminder.dao.mapper.GlobalReminderMapper;
import codedriver.framework.reminder.dto.GlobalReminderSubscribeVo;
import codedriver.framework.reminder.dto.GlobalReminderHandlerVo;

/**
 * @program: codedriver
 * @description:
 * @create: 2019-12-10 11:35
 **/
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ReminderSubscribeSearchApi extends PrivateApiComponentBase {
   
    @Autowired
    private GlobalReminderMapper reminderMapper;

    @Override
    public String getToken() {
        return "globalReminder/subscribe/search";
    }

    @Override
    public String getName() {
        return "获取订阅设置通知列表接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param(name = "moduleId", type = ApiParamType.STRING, desc = "模块ID")})
    @Output({@Param(explode = GlobalReminderHandlerVo.class),
             @Param(name = "reminderList", explode = GlobalReminderHandlerVo[].class, desc = "实时动态插件集合")})
    @Description(desc = "获取订阅设置通知列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        GlobalReminderHandlerVo reminderVo = new GlobalReminderHandlerVo();
        reminderVo.setModuleId(jsonObj.getString("moduleId"));
        JSONObject returnJson = new JSONObject();
        boolean moduleId = StringUtils.isNotBlank(reminderVo.getModuleId());
        List<GlobalReminderHandlerVo> activeReminderList = new ArrayList<>();
        List<GlobalReminderHandlerVo> reminderHandlerList = GlobalReminderHandlerFactory.getReminderHandlerList();
        Map<String, ModuleVo> moduleVoMap = TenantContext.get().getActiveModuleMap();
        for (GlobalReminderHandlerVo handler : reminderHandlerList) {
            if (moduleVoMap.containsKey(handler.getModuleId())) {
                handler.setModuleName(moduleVoMap.get(handler.getModuleId()).getName());
                handler.setModuleName(moduleVoMap.get(handler.getModuleId()).getDescription());
                if (!moduleId || (moduleId && handler.getModuleId().equals(reminderVo.getModuleId()))) {
                    activeReminderList.add(handler);
                }
            }
        }
        List<GlobalReminderSubscribeVo> reminderSubscribeList = reminderMapper.getReminderSubscribeListByUserUuid(UserContext.get().getUserUuid(true));
        Map<String, GlobalReminderSubscribeVo> subscribeMap = new HashMap<>();
        for (GlobalReminderSubscribeVo subscribeVo : reminderSubscribeList) {
            subscribeMap.put(subscribeVo.getHandler(), subscribeVo);
        }
        for (GlobalReminderHandlerVo handler : activeReminderList) {
            if (subscribeMap.containsKey(handler.getHandler())) {
            	handler.setReminderSubscribeVo(subscribeMap.get(handler.getHandler()));
            }
        }
        Collections.sort(reminderHandlerList);
        returnJson.put("tbodyList", reminderHandlerList);
        return returnJson;
    }
}
