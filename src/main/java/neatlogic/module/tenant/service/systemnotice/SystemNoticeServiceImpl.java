package neatlogic.module.tenant.service.systemnotice;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.UserType;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.systemnotice.dao.mapper.SystemNoticeMapper;
import neatlogic.framework.systemnotice.dto.SystemNoticeUserVo;
import neatlogic.framework.systemnotice.dto.SystemNoticeVo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Title: SystemNoticeServiceImpl
 * @Package: neatlogic.framework.systemnotice.service
 * @Description:
 * @Author: laiwt
 * @Date: 2021/1/20 11:02
 **/
@Service
public class SystemNoticeServiceImpl implements SystemNoticeService{

    @Autowired
    private SystemNoticeMapper systemNoticeMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private TeamMapper teamMapper;

    @Override
    public void clearSystemNoticeUser() {
        List<String> recipientUuidList = getRecipientUuidList();
        Set<Long> noticeIdList = new HashSet<>();
        /** 获取system_notice_user中，因公告被删除而残留的记录 **/
        noticeIdList.addAll(systemNoticeMapper.getNotExistsNoticeIdListFromNoticeUserByUserUuid(UserContext.get().getUserUuid(true)));
        /** 获取system_notice_user中，因更改公告通知对象而残留的记录  **/
        noticeIdList.addAll(systemNoticeMapper.getNotInNoticeScopeNoticeIdListByUserUuid(recipientUuidList,UserContext.get().getUserUuid(true)));
        /** 清理掉上述两种记录 **/
        if(CollectionUtils.isNotEmpty(noticeIdList)){
            systemNoticeMapper.deleteSystemNoticeUserByUserUuid(UserContext.get().getUserUuid(true),noticeIdList);
        }
    }

    @Override
    public void stopExpiredSystemNotice() {
        List<String> recipientUuidList = getRecipientUuidList();
        List<SystemNoticeVo> expiredNoticeList = systemNoticeMapper.getExpiredNoticeListByRecipientUuidList(recipientUuidList);
        if (CollectionUtils.isNotEmpty(expiredNoticeList)) {
            for (SystemNoticeVo vo : expiredNoticeList) {
                systemNoticeMapper.stopSystemNoticeById(vo);
            }
        }
    }

    @Override
    public void pullIssuedSystemNotice() {
        List<String> recipientUuidList = getRecipientUuidList();
        List<Long> issuedNoticeList = systemNoticeMapper.getIssuedNoticeIdListByRecipientUuidList(recipientUuidList);
        if (CollectionUtils.isNotEmpty(issuedNoticeList)) {
            List<SystemNoticeUserVo> noticeUserVoList = new ArrayList<>();
            for (Long id : issuedNoticeList) {
                noticeUserVoList.add(new SystemNoticeUserVo(id, UserContext.get().getUserUuid(true)));
            }
            systemNoticeMapper.batchInsertSystemNoticeUser(noticeUserVoList);
        }
    }

    @Override
    public void pullActiveSystemNotice() {
        List<String> recipientUuidList = getRecipientUuidList();
        List<SystemNoticeVo> hasBeenActiveNoticeList = systemNoticeMapper.getHasBeenActiveNoticeListByRecipientUuidList(recipientUuidList);
        if (CollectionUtils.isNotEmpty(hasBeenActiveNoticeList)) {
            List<SystemNoticeUserVo> currentUserNoticeList = new ArrayList<>();
            /** 更改这些公告的状态为已下发 **/
            for (SystemNoticeVo vo : hasBeenActiveNoticeList) {
                vo.setStatus(SystemNoticeVo.Status.ISSUED.getValue());
                vo.setIssueTime(vo.getStartTime());
                systemNoticeMapper.updateSystemNoticeStatus(vo);
                currentUserNoticeList.add(new SystemNoticeUserVo(vo.getId(), UserContext.get().getUserUuid(true)));
                /** 如果没有忽略已读，那么更改is_read为0 **/
                if(vo.getIgnoreRead() != null && vo.getIgnoreRead() == 0){
                    systemNoticeMapper.updateSystemNoticeUserReadStatus(vo.getId(),UserContext.get().getUserUuid(true),0);
                }
            }
            /** 发送给当前用户 **/
            if (CollectionUtils.isNotEmpty(currentUserNoticeList)) {
                systemNoticeMapper.batchInsertSystemNoticeUser(currentUserNoticeList);
            }
        }
    }

    private List<String> getRecipientUuidList(){
        List<String> uuidList = new ArrayList<>();
        uuidList.add(UserContext.get().getUserUuid(true));
        uuidList.add(UserType.ALL.getValue());
        uuidList.addAll(teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true)));
        uuidList.addAll(roleMapper.getRoleUuidListByUserUuid(UserContext.get().getUserUuid(true)));
        return uuidList;
    }
}
