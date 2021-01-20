package codedriver.module.tenant.service.systemnotice;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.UserType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.systemnotice.dao.mapper.SystemNoticeMapper;
import codedriver.framework.systemnotice.dto.SystemNoticeUserVo;
import codedriver.framework.systemnotice.dto.SystemNoticeVo;
import codedriver.framework.transaction.util.TransactionUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * @Title: SystemNoticeServiceImpl
 * @Package: codedriver.framework.systemnotice.service
 * @Description:
 * @Author: laiwt
 * @Date: 2021/1/20 11:02
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
@Service
public class SystemNoticeServiceImpl implements SystemNoticeService{

    @Autowired
    private SystemNoticeMapper systemNoticeMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void clearSystemNoticeUser() {
        /** 清理掉system_notice_user中，因公告被删除而残留的记录 **/
        systemNoticeMapper.deleteNotExistsNoticeByUserUuid(UserContext.get().getUserUuid(true));

        /** 清理掉system_notice_user中，因更改公告通知对象而残留的记录 **/
        List<String> uuidList = new ArrayList<>();
        uuidList.add(UserContext.get().getUserUuid(true));
        uuidList.add(UserType.ALL.getValue());
        uuidList.addAll(userMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true)));
        uuidList.addAll(userMapper.getRoleUuidListByUserUuid(UserContext.get().getUserUuid(true)));

        systemNoticeMapper.deleteNoticeUserWhoIsNotInNoticeScope(UserContext.get().getUserUuid(true),uuidList);
    }

    @Override
    public void stopExpiredSystemNotice(List<String> uuidList) {
        List<SystemNoticeVo> expiredNoticeList = systemNoticeMapper.getExpiredNoticeListByRecipientUuidList(uuidList);
        if (CollectionUtils.isNotEmpty(expiredNoticeList)) {
            for (SystemNoticeVo vo : expiredNoticeList) {
                vo.setStatus(SystemNoticeVo.Status.STOPPED.getValue());
                systemNoticeMapper.updateSystemNoticeStatus(vo);
            }
        }
    }

    @Override
    public void pullIssuedSystemNotice(List<String> uuidList) {
        List<Long> issuedNoticeList = systemNoticeMapper.getIssuedNoticeIdListByRecipientUuidList(uuidList);
        if (CollectionUtils.isNotEmpty(issuedNoticeList)) {
            List<SystemNoticeUserVo> noticeUserVoList = new ArrayList<>();
            for (Long id : issuedNoticeList) {
                noticeUserVoList.add(new SystemNoticeUserVo(id, UserContext.get().getUserUuid(true)));
            }
            systemNoticeMapper.batchInsertSystemNoticeUser(noticeUserVoList);
        }
    }

    @Override
    public void pullActiveSystemNotice(List<String> uuidList) {
        List<SystemNoticeVo> hasBeenActiveNoticeList = systemNoticeMapper.getHasBeenActiveNoticeListByRecipientUuidList(uuidList);
        if (CollectionUtils.isNotEmpty(hasBeenActiveNoticeList)) {
            TransactionStatus transactionStatus = TransactionUtil.openTx();
            List<SystemNoticeUserVo> currentUserNoticeList = new ArrayList<>();
            /** 更改这些公告的状态为已发布 **/
            for (SystemNoticeVo vo : hasBeenActiveNoticeList) {
                vo.setStatus(SystemNoticeVo.Status.ISSUED.getValue());
                vo.setIssueTime(vo.getStartTime());
                systemNoticeMapper.updateSystemNoticeStatus(vo);
                currentUserNoticeList.add(new SystemNoticeUserVo(vo.getId(), UserContext.get().getUserUuid(true)));
            }
            /** 发送给当前用户 **/
            if (CollectionUtils.isNotEmpty(currentUserNoticeList)) {
                systemNoticeMapper.batchInsertSystemNoticeUser(currentUserNoticeList);
            }
            TransactionUtil.commitTx(transactionStatus);
        }
    }
}
