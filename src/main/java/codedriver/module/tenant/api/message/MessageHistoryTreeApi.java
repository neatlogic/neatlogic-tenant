package codedriver.module.tenant.api.message;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.message.dao.mapper.MessageMapper;
import codedriver.framework.message.dto.MessageSearchVo;
import codedriver.framework.message.dto.TriggerMessageCountVo;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.dto.NotifyTreeVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.TimeUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Title: MessageHistoryTreeApi
 * @Package codedriver.module.tenant.api.message
 * @Description: 查询历史消息分类树
 * @Author: linbq
 * @Date: 2021/2/22 14:28
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
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
        List<NotifyTreeVo> triggerTreeVoList = new ArrayList<>();
        /** 复制分类树，并收集叶子节点 **/
        List<NotifyTreeVo> resultList = copy(moduleTreeVoList, triggerTreeVoList);

        MessageSearchVo searchVo = JSONObject.toJavaObject(jsonObj, MessageSearchVo.class);
        if (searchVo.getStartTime() == null && searchVo.getEndTime() == null) {
            Integer timeRange = jsonObj.getInteger("timeRange");
            String timeUnit = jsonObj.getString("timeUnit");
            if (timeRange != null && StringUtils.isNotBlank(timeUnit)) {
                searchVo.setStartTime(TimeUtil.recentTimeTransfer(timeRange, timeUnit));
                searchVo.setEndTime(new Date());
            }
        }
        if (searchVo.getStartTime() == null || searchVo.getEndTime() == null) {
            return resultList;
        }
        searchVo.setUserUuid(UserContext.get().getUserUuid(true));
        List<TriggerMessageCountVo> triggerMessageCountVoList = messageMapper.getTriggerMessageCountListGroupByTriggerAndIsRead(searchVo);
        Map<String, Integer> triggerMessageUnreadCountMap = new HashMap<>();
        Map<String, Integer> triggerMessageReadCountMap = new HashMap<>();
        for(TriggerMessageCountVo triggerMessageCountVo : triggerMessageCountVoList){
            if(Objects.equals(triggerMessageCountVo.getIsRead(), 0)){
                triggerMessageUnreadCountMap.put(triggerMessageCountVo.getTrigger(), triggerMessageCountVo.getCount());
            }else if(Objects.equals(triggerMessageCountVo.getIsRead(), 1)){
                triggerMessageReadCountMap.put(triggerMessageCountVo.getTrigger(), triggerMessageCountVo.getCount());
            }
        }

        for(NotifyTreeVo treeVo : triggerTreeVoList){
            Integer unreadCount = triggerMessageUnreadCountMap.get(treeVo.getUuid());
            if(unreadCount == null){
                unreadCount = 0;
            }treeVo.setUnreadCount(unreadCount);
            Integer readCount = triggerMessageReadCountMap.get(treeVo.getUuid());
            if(readCount == null){
                readCount = 0;
            }
            treeVo.setTotal(unreadCount + readCount);
        }
        return resultList;
    }

    private List<NotifyTreeVo> copy(List<NotifyTreeVo> notifyTreeVoList, List<NotifyTreeVo> triggerTreeVoList){
        List<NotifyTreeVo> resultList = new ArrayList<>(notifyTreeVoList.size());
        for(NotifyTreeVo notifyTreeVo : notifyTreeVoList){
            NotifyTreeVo newNotifyTreeVo = new NotifyTreeVo(notifyTreeVo.getUuid(), notifyTreeVo.getName());
            resultList.add(newNotifyTreeVo);
            if(notifyTreeVo.getChildren() != null){
                newNotifyTreeVo.setChildren(copy(notifyTreeVo.getChildren(), triggerTreeVoList));
            }else{
                triggerTreeVoList.add(newNotifyTreeVo);
            }
        }
        return resultList;
    }
}
