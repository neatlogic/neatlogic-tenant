package codedriver.module.tenant.service.message;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.message.dao.mapper.MessageMapper;
import codedriver.framework.message.dto.MessageSearchVo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @Title: MessageServiceImpl
 * @Package codedriver.module.tenant.service.message
 * @Description: 消息通知Service实现类
 * @Author: linbq
 * @Date: 2021/1/5 14:26
 **/
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TeamMapper teamMapper;
    @Override
    public List<Long> pullMessage(MessageSearchVo searchVo) {
        searchVo.setUserUuid(UserContext.get().getUserUuid(true));
        searchVo.setRoleUuidList(userMapper.getRoleUuidListByUserUuid(UserContext.get().getUserUuid(true)));
        searchVo.setTeamUuidList(teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true)));

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - 7);
        Date earliestSendingTime = calendar.getTime();

        Date lastPullTime = null;
        Long maxNewsMessageId = messageMapper.getMessageUserMaxMessageIdByUserUuid(UserContext.get().getUserUuid(true));
        if (maxNewsMessageId != null) {
            lastPullTime = messageMapper.getMessageFcdById(maxNewsMessageId);
        }
        if (lastPullTime == null || lastPullTime.before(earliestSendingTime)) {
            searchVo.setStartTime(earliestSendingTime);
        } else {
            searchVo.setStartTime(lastPullTime);
        }
        if(searchVo.getNeedPage()){
            int rowNum = messageMapper.getMessagePullCount(searchVo);
            searchVo.setRowNum(rowNum);
            if(rowNum > 0){
                searchVo.setPageCount(PageUtil.getPageCount(rowNum, searchVo.getPageSize()));
                List<Long> messageIdList = messageMapper.getMessagePullList(searchVo);
                insertNewsMessageUserList(messageIdList);
                return messageIdList;
            }
        }else{
            List<Long> messageIdList = messageMapper.getMessagePullList(searchVo);
            insertNewsMessageUserList(messageIdList);
            return messageIdList;
        }
        return null;
    }

    /**
     * @Description: 保存用户拉取到的新消息id
     * @Author: linbq
     * @Date: 2021/1/5 14:36
     * @Params:[messageIdList]
     * @Returns:void
     **/
    private void insertNewsMessageUserList(List<Long> messageIdList) {
        int size = Math.min(1000, messageIdList.size());
        List<MessageSearchVo> messageSearchVoList = new ArrayList<>(size);
        String userUuid = UserContext.get().getUserUuid(true);
        for (Long messageId : messageIdList) {
            messageSearchVoList.add(new MessageSearchVo(userUuid, messageId));
            if (messageSearchVoList.size() == 1000) {
                messageMapper.insertMessageUser(messageSearchVoList);
                messageSearchVoList.clear();
            }
        }
        if (CollectionUtils.isNotEmpty(messageSearchVoList)) {
            messageMapper.insertMessageUser(messageSearchVoList);
        }
    }
}
