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

package neatlogic.module.tenant.api.message;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.message.dao.mapper.MessageMapper;
import neatlogic.framework.message.dto.MessageSearchVo;
import neatlogic.framework.message.dto.MessageVo;
import neatlogic.framework.notify.core.NotifyPolicyHandlerFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TimeUtil;
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
        return "????????????????????????";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, xss = true, desc = "???????????????????????????"),
            @Param(name = "messageTypePath", type = ApiParamType.JSONARRAY, desc = "??????????????????"),
            @Param(name = "startTime", type = ApiParamType.LONG, desc = "????????????"),
            @Param(name = "endTime", type = ApiParamType.LONG, desc = "????????????"),
            @Param(name = "timeRange", type = ApiParamType.INTEGER, desc = "????????????"),
            @Param(name = "timeUnit", type = ApiParamType.ENUM, rule = "year,month,week,day,hour", desc = "??????????????????"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "????????????"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "????????????")
    })
    @Output({
            @Param(name = "tbodyList", explode = MessageVo[].class, desc = "????????????"),
            @Param(name = "unreadCount", type = ApiParamType.INTEGER, desc = "??????????????????"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "????????????????????????")
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
