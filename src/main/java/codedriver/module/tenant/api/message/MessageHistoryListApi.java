/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.message;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.message.dao.mapper.MessageMapper;
import codedriver.framework.message.dto.MessageSearchVo;
import codedriver.framework.message.dto.MessageVo;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.TimeUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
            @Param(name = "keyword", type = ApiParamType.STRING, xss = true, desc = "消息标题，模糊搜索"),
            @Param(name = "messageTypePath", type = ApiParamType.JSONARRAY, desc = "消息分类路径"),
            @Param(name = "startTime", type = ApiParamType.LONG, desc = "开始时间"),
            @Param(name = "endTime", type = ApiParamType.LONG, desc = "结束时间"),
            @Param(name = "timeRange", type = ApiParamType.INTEGER, desc = "时间范围"),
            @Param(name = "timeUnit", type = ApiParamType.ENUM, rule = "year,month,week,day,hour", desc = "时间范围单位"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条数")
    })
    @Output({
            @Param(name = "tbodyList", explode = MessageVo[].class, desc = "消息列表"),
            @Param(name = "unreadCount", type = ApiParamType.INTEGER, desc = "未读消息数量"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "查询历史消息列表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        List<MessageVo> messageVoList = new ArrayList<>();
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
            resultObj.put("currentPage", searchVo.getCurrentPage());
            resultObj.put("pageSize", searchVo.getPageSize());
            resultObj.put("pageCount", 0);
            resultObj.put("rowNum", 0);
            resultObj.put("tbodyList", messageVoList);
            return resultObj;
        }

        JSONArray messageTypePath = jsonObj.getJSONArray("messageTypePath");
        List<String> triggerList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(messageTypePath)) {
            if (messageTypePath.size() == 1) {
                searchVo.setTriggerList(NotifyPolicyHandlerFactory.getTriggerList(messageTypePath.getString(0)));
            } else if (messageTypePath.size() == 2) {
                searchVo.setNotifyPolicyHandler(messageTypePath.getString(1));
            } else if (messageTypePath.size() == 3) {
                searchVo.setNotifyPolicyHandler(messageTypePath.getString(1));
                triggerList.add(messageTypePath.getString(2));
            }
        } else {
            triggerList = NotifyPolicyHandlerFactory.getAllActiveTriggerList();
        }
        searchVo.setTriggerList(triggerList);

        searchVo.setUserUuid(UserContext.get().getUserUuid(true));
        int unreadCount = 0;
        int pageCount = 0;
        int rowNum = messageMapper.getMessageHistoryCount(searchVo);
        if (rowNum > 0) {
            pageCount = PageUtil.getPageCount(rowNum, searchVo.getPageSize());
            if (searchVo.getCurrentPage() <= pageCount) {
                messageVoList = messageMapper.getMessageHistoryList(searchVo);
            }
            searchVo.setIsRead(0);
            unreadCount = messageMapper.getMessageHistoryCount(searchVo);
        }
        resultObj.put("currentPage", searchVo.getCurrentPage());
        resultObj.put("pageSize", searchVo.getPageSize());
        resultObj.put("pageCount", pageCount);
        resultObj.put("rowNum", rowNum);
        resultObj.put("unreadCount", unreadCount);
        resultObj.put("tbodyList", messageVoList);
        return resultObj;
    }
}
