package codedriver.module.tenant.api.message;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.message.dao.mapper.MessageMapper;
import codedriver.framework.message.dto.TriggerMessageCountVo;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.dto.NotifyTreeVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    @Output({
            @Param(name = "list", explode = NotifyTreeVo[].class, desc = "查询历史消息分类树")
    })
    @Description(desc = "查询历史消息分类树")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<TriggerMessageCountVo> triggerMessageCountVoList = messageMapper.getTriggerMessageCountListGroupByTrigger(UserContext.get().getUserUuid(true));
        Map<String, Integer> triggerMessageCountMap = triggerMessageCountVoList.stream().collect(Collectors.toMap(e -> e.getTrigger(), e -> e.getCount()));
        List<NotifyTreeVo> moduleTreeVoList = NotifyPolicyHandlerFactory.getModuleTreeVoList();
        List<NotifyTreeVo> triggerTreeVoList = new ArrayList<>();
                /** 复制分类树，并收集叶子节点 **/
        List<NotifyTreeVo> resultList = copy(moduleTreeVoList, triggerTreeVoList);

        for(NotifyTreeVo treeVo : triggerTreeVoList){
            Integer count = triggerMessageCountMap.get(treeVo.getUuid());
            if(count == null){
                treeVo.setCount(0);
            }else{
                treeVo.setCount(count);
            }
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
