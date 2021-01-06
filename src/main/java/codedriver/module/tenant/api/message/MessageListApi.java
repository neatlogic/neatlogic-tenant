package codedriver.module.tenant.api.message;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.message.dao.mapper.MessageMapper;
import codedriver.framework.message.dto.MessageSearchVo;
import codedriver.framework.message.dto.MessageVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Title: MessageListApi
 * @Package codedriver.module.tenant.api.message
 * @Description: 查询消息列表接口
 * @Author: linbq
 * @Date: 2020/12/31 16:17
 **/
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class MessageListApi extends PrivateApiComponentBase {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public String getToken() {
        return "message/list";
    }
    @Override
    public String getName() {
        return "查询消息列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "messageId", type = ApiParamType.LONG, desc = "起点消息id"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条数"),
            @Param(name = "needPage", type = ApiParamType.INTEGER, desc = "是否分页")
    })
    @Output({
            @Param(name = "tbodyList", explode = MessageVo[].class, desc = "消息列表"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "查询消息列表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        List<MessageVo> messageVoList = new ArrayList<>();
        MessageSearchVo searchVo = JSONObject.toJavaObject(jsonObj, MessageSearchVo.class);
        searchVo.setUserUuid(UserContext.get().getUserUuid(true));
        if(searchVo.getNeedPage()){
            int pageCount = 0;
            int rowNum = messageMapper.getMessageCount(searchVo);
            if(rowNum > 0){
                pageCount = PageUtil.getPageCount(rowNum, searchVo.getPageSize());
                if(searchVo.getCurrentPage() <= pageCount){
                    messageVoList = messageMapper.getMessageList(searchVo);
                }
            }
            resultObj.put("currentPage", searchVo.getCurrentPage());
            resultObj.put("pageSize", searchVo.getPageSize());
            resultObj.put("pageCount", pageCount);
            resultObj.put("rowNum", rowNum);
        }else{
            messageVoList = messageMapper.getMessageList(searchVo);
        }
        resultObj.put("tbodyList", messageVoList);
        return resultObj;
    }
}
