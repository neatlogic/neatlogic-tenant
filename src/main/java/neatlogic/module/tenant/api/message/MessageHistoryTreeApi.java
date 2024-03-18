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

package neatlogic.module.tenant.api.message;

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.message.dao.mapper.MessageMapper;
import neatlogic.framework.message.dto.MessageSearchVo;
import neatlogic.framework.message.dto.TriggerMessageCountVo;
import neatlogic.framework.notify.core.NotifyPolicyHandlerFactory;
import neatlogic.framework.notify.dto.NotifyTreeVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TimeUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class MessageHistoryTreeApi extends PrivateApiComponentBase {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public String getToken() {
        return "message/history/tree";
    }

    @Override
    public String getName() {
        return "查询历史消息分类树";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, xss = true, desc = "消息标题，模糊搜索"),
            @Param(name = "startTime", type = ApiParamType.LONG, desc = "开始时间"),
            @Param(name = "endTime", type = ApiParamType.LONG, desc = "结束时间"),
            @Param(name = "timeRange", type = ApiParamType.INTEGER, desc = "时间范围"),
            @Param(name = "timeUnit", type = ApiParamType.ENUM, rule = "year,month,week,day,hour", desc = "时间范围单位")
    })
    @Output({
            @Param(name = "list", explode = NotifyTreeVo[].class, desc = "查询历史消息分类树")
    })
    @Description(desc = "查询历史消息分类树")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String userUuid = UserContext.get().getUserUuid(true);
        List<NotifyTreeVo> moduleTreeVoList = NotifyPolicyHandlerFactory.getModuleTreeVoList();
        //仅展示该租户已激活的模块组
        moduleTreeVoList = moduleTreeVoList.stream().filter(t->TenantContext.get().getActiveModuleList().stream().anyMatch(o->Objects.equals(o.getGroup(),t.getUuid()))).collect(Collectors.toList());
        MessageSearchVo searchVo = JSONObject.toJavaObject(jsonObj, MessageSearchVo.class);
        if (searchVo.getStartTime() == null && searchVo.getEndTime() == null) {
            Integer timeRange = jsonObj.getInteger("timeRange");
            String timeUnit = jsonObj.getString("timeUnit");
            if (timeRange != null && StringUtils.isNotBlank(timeUnit)) {
                searchVo.setStartTime(TimeUtil.recentTimeTransfer(timeRange, timeUnit));
                searchVo.setEndTime(new Date());
            }
        }

        Map<String, Integer> triggerMessageUnreadCountMap = new HashMap<>();
        Map<String, Integer> triggerMessageReadCountMap = new HashMap<>();
        if (searchVo.getStartTime() != null && searchVo.getEndTime() != null) {
            searchVo.setUserUuid(UserContext.get().getUserUuid(true));
            List<TriggerMessageCountVo> triggerMessageCountVoList = messageMapper.getTriggerMessageCountListGroupByTriggerAndIsRead(searchVo);
            for(TriggerMessageCountVo triggerMessageCountVo : triggerMessageCountVoList){
                String key = triggerMessageCountVo.getNotifyPolicyHandler() + "#" + triggerMessageCountVo.getTrigger();
                if(Objects.equals(triggerMessageCountVo.getIsRead(), 0)){
                    triggerMessageUnreadCountMap.put(key, triggerMessageCountVo.getCount());
                }else if(Objects.equals(triggerMessageCountVo.getIsRead(), 1)){
                    triggerMessageReadCountMap.put(key, triggerMessageCountVo.getCount());
                }
            }
        }
        /** 复制分类树，并收集叶子节点 **/
        List<NotifyTreeVo> resultList = copy(moduleTreeVoList, "", triggerMessageUnreadCountMap, triggerMessageReadCountMap);

        return resultList;
    }

    private List<NotifyTreeVo> copy(List<NotifyTreeVo> notifyTreeVoList, String parentUuid, Map<String, Integer> triggerMessageUnreadCountMap, Map<String, Integer> triggerMessageReadCountMap){
        List<NotifyTreeVo> resultList = new ArrayList<>(notifyTreeVoList.size());
        for(NotifyTreeVo notifyTreeVo : notifyTreeVoList){
            NotifyTreeVo newNotifyTreeVo = new NotifyTreeVo(notifyTreeVo.getUuid(), notifyTreeVo.getName());
            if(notifyTreeVo.getChildren() != null){
                List<NotifyTreeVo> children = copy(notifyTreeVo.getChildren(), newNotifyTreeVo.getUuid(), triggerMessageUnreadCountMap, triggerMessageReadCountMap);
                if(CollectionUtils.isNotEmpty(children)){
                    newNotifyTreeVo.setChildren(children);
                    resultList.add(newNotifyTreeVo);
                }
            }else{
                String key = parentUuid + "#" + newNotifyTreeVo.getUuid();
                Integer unreadCount = triggerMessageUnreadCountMap.get(key);
                if(unreadCount == null){
                    unreadCount = 0;
                }
                newNotifyTreeVo.setUnreadCount(unreadCount);
                Integer readCount = triggerMessageReadCountMap.get(key);
                if(readCount == null){
                    readCount = 0;
                }
                newNotifyTreeVo.setTotal(unreadCount + readCount);
                if(newNotifyTreeVo.getTotal() > 0){
                    resultList.add(newNotifyTreeVo);
                }
            }
        }
        return resultList;
    }
}
