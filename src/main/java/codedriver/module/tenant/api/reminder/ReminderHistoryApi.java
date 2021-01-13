package codedriver.module.tenant.api.reminder;

import java.util.ArrayList;
import java.util.List;

import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.reminder.core.GlobalReminderHandlerFactory;
import codedriver.framework.reminder.core.IGlobalReminderHandler;
import codedriver.framework.reminder.dao.mapper.GlobalReminderMessageMapper;
import codedriver.framework.reminder.dto.GlobalReminderHandlerVo;
import codedriver.framework.reminder.dto.GlobalReminderMessageVo;
import codedriver.framework.reminder.dto.param.ReminderHistoryParamVo;
import codedriver.framework.util.TimeUtil;
import codedriver.module.tenant.service.reminder.GlobalReminderService;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-09 15:04
 **/
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ReminderHistoryApi extends PrivateApiComponentBase {

	@Autowired
	private GlobalReminderService reminderService;
	
	@Autowired
	private GlobalReminderMessageMapper reminderMessageMapper;

    @Override
    public String getToken() {
        return "globalReminder/history";
    }

    @Override
    public String getName() {
        return "实时动态历史消息查看接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param( name = "moduleId", desc = "模块ID", type = ApiParamType.STRING),
            @Param( name = "timeRange", desc = "时间", type = ApiParamType.INTEGER),
            @Param( name = "timeUnit", desc = "时间单位", type = ApiParamType.STRING),
            @Param( name = "startTime", desc = "起始时间", type = ApiParamType.STRING),
            @Param( name = "endTime", desc = "结束时间", type = ApiParamType.STRING),
            @Param( name = "pageSize", desc = "每页条目数", type = ApiParamType.INTEGER),
            @Param( name = "currentPage", desc = "当前页码", type = ApiParamType.INTEGER)
    })
    @Output({
            @Param( name = "messageList", desc = "消息集合列表", type = ApiParamType.JSONARRAY),
            @Param( name = "currentPage", desc = "当前页码", type = ApiParamType.INTEGER),
            @Param( name = "rowNum", desc = "数目总数", type = ApiParamType.INTEGER),
            @Param( name = "pageSize", desc = "最大条目", type = ApiParamType.INTEGER)
    })
    @Description( desc = "实时动态历史消息查看接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        ReminderHistoryParamVo paramVo = new ReminderHistoryParamVo();

        if (jsonObj.containsKey("timeRange")){
            paramVo.setEndTime(TimeUtil.timeNow());
            paramVo.setStartTime(TimeUtil.timeTransfer(jsonObj.getInteger("timeRange"), jsonObj.getString("timeUnit")));
        }else {
           paramVo.setStartTime(jsonObj.getString("startTime"));
           paramVo.setEndTime(jsonObj.getString("endTime"));
        }
        if (jsonObj.containsKey("pageSize")){
            paramVo.setPageSize(jsonObj.getInteger("pageSize"));
        }
        if (jsonObj.containsKey("currentPage")){
            paramVo.setCurrentPage(jsonObj.getInteger("currentPage"));
        }

        paramVo.setModuleId(jsonObj.getString("moduleId"));
        paramVo.setUserUuid(UserContext.get().getUserUuid(true));
        List<GlobalReminderMessageVo> messageList = getReminderHistoryMessageList(paramVo);
        JSONArray messageArray = new JSONArray();
        if (CollectionUtils.isNotEmpty(messageList)){
            for (GlobalReminderMessageVo messageVo : messageList){
                IGlobalReminderHandler reminder = GlobalReminderHandlerFactory.getReminder(messageVo.getReminderVo().getHandler());
                messageArray.add(reminder.packData(messageVo));
            }
        }
        returnObj.put("messageList", messageArray);
        returnObj.put("currentPage", paramVo.getCurrentPage());
        returnObj.put("rowNum", paramVo.getRowNum());
        returnObj.put("pageSize", paramVo.getPageSize());
        return returnObj;
    }
    
    /** 
     * @Description: 获取历史实时动态消息 
     * @Param: [paramVo] 
     * @return: java.util.List<codedriver.framework.reminder.dto.GlobalReminderMessageVo>  
     */ 
    public List<GlobalReminderMessageVo> getReminderHistoryMessageList(ReminderHistoryParamVo paramVo) {
        List<GlobalReminderHandlerVo> reminderHandlerList = GlobalReminderHandlerFactory.getReminderHandlerList();
        if (CollectionUtils.isNotEmpty(reminderHandlerList)){
            List<String> handlerList = new ArrayList<>();
            for (GlobalReminderHandlerVo reminderHandler : reminderHandlerList){
                if (reminderHandler.getModuleId().equals(paramVo.getModuleId())){
                	handlerList.add(reminderHandler.getHandler());
                }
            }
            paramVo.setHandlerList(handlerList);
        }

        if (paramVo.getNeedPage()){
            int rowNum = reminderMessageMapper.getReminderHistoryMessageCount(paramVo);
            paramVo.setRowNum(rowNum);
            paramVo.setPageCount(PageUtil.getPageCount(rowNum, paramVo.getPageSize()));
        }
        List<GlobalReminderMessageVo> messageVoList = reminderMessageMapper.getReminderHistoryMessageList(paramVo);

        if (CollectionUtils.isNotEmpty(messageVoList)){
            for (GlobalReminderMessageVo messageVo : messageVoList){
            	reminderService.packageData(messageVo);
            }
        }
        return messageVoList;
    }
}
