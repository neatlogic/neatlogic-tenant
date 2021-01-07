package codedriver.module.tenant.api.message;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.message.core.MessageHandlerFactory;
import codedriver.framework.message.dao.mapper.MessageMapper;
import codedriver.framework.message.dto.MessageHandlerVo;
import codedriver.framework.message.dto.MessageSearchVo;
import codedriver.framework.message.dto.MessageVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.TimeUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Title: MessageHistoryListApi
 * @Package codedriver.module.tenant.api.message
 * @Description: 查询历史消息列表接口
 * @Author: linbq
 * @Date: 2021/1/4 11:05
 * Copyright(c) 2020 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class MessageHistoryListApi extends PrivateApiComponentBase {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public String getToken() {
        return "message/history/list";
    }

    @Override
    public String getName() {
        return "查询历史消息列表";
    }

    @Override
    public String getConfig() {
        return null;
    }
    @Input({
            @Param(name = "moduleId", type = ApiParamType.STRING, desc = "模块id"),
            @Param(name = "startTime", type = ApiParamType.LONG, desc = "开始时间"),
            @Param(name = "endTime", type = ApiParamType.LONG, desc = "结束时间"),
            @Param(name = "timeRange", type = ApiParamType.INTEGER, desc = "模块id"),
            @Param(name = "timeUnit", type = ApiParamType.ENUM, rule = "year,month,week,day,hour", desc = "模块id"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条数"),
            @Param(name = "needPage", type = ApiParamType.INTEGER, desc = "是否分页")
    })
    @Output({
            @Param(name = "tbodyList", explode = MessageVo[].class, desc = "消息列表"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "查询历史消息列表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        List<MessageVo> messageVoList = new ArrayList<>();
        MessageSearchVo searchVo = JSONObject.toJavaObject(jsonObj, MessageSearchVo.class);
        String moduleId = jsonObj.getString("moduleId");
        if(StringUtils.isNotBlank(moduleId)){
            List<String> handlerList = new ArrayList<>();
            for (MessageHandlerVo messageHandlerVo : MessageHandlerFactory.getMessageHandlerVoList()) {
                if(moduleId.equals(messageHandlerVo.getModuleId())){
                    handlerList.add(messageHandlerVo.getHandler());
                }
            }
            searchVo.setHandlerList(handlerList);
        }
        if(searchVo.getStartTime() == null && searchVo.getEndTime() == null){
            Integer timeRange = jsonObj.getInteger("timeRange");
            String timeUnit = jsonObj.getString("timeUnit");
            if(timeRange != null && StringUtils.isNotBlank(timeUnit)){
                searchVo.setStartTime(TimeUtil.recentTimeTransfer(timeRange, timeUnit));
                searchVo.setEndTime(new Date());
            }
        }
        searchVo.setUserUuid(UserContext.get().getUserUuid(true));
        if(searchVo.getNeedPage()){
            int pageCount = 0;
            int rowNum = messageMapper.getMessageHistoryCount(searchVo);
            if(rowNum > 0){
                pageCount = PageUtil.getPageCount(rowNum, searchVo.getPageSize());
                if(searchVo.getCurrentPage() <= pageCount){
                    messageVoList = messageMapper.getMessageHistoryList(searchVo);
                }
            }
            resultObj.put("currentPage", searchVo.getCurrentPage());
            resultObj.put("pageSize", searchVo.getPageSize());
            resultObj.put("pageCount", pageCount);
            resultObj.put("rowNum", rowNum);
        }else{
            messageVoList = messageMapper.getMessageHistoryList(searchVo);
        }
        resultObj.put("tbodyList", messageVoList);
        return resultObj;
    }
}
