package neatlogic.module.tenant.service.systemnotice;

import neatlogic.framework.login.core.LoginPostProcessorBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Title: LoginPullSystemNoticeProcessor
 * @Package neatlogic.framework.message.login.handler
 * @Description: 登录后拉取系统公告处理器
 * @Author: laiwt
 * @Date: 2021/1/15 15:38
 **/
@Service
public class LoginPullSystemNoticeProcessor extends LoginPostProcessorBase {

    @Autowired
    private SystemNoticeService systemNoticeService;

    @Override
    protected void myLoginAfterInitialization() {

        /** 清理掉system_notice_user中因删除公告或更改公告通知对象而遗留的记录 **/
        systemNoticeService.clearSystemNoticeUser();

        /** 检查是否存在【已下发却到了失效时间的】公告，如果有，则停用 **/
        systemNoticeService.stopExpiredSystemNotice();

        /** 在system_notice_user插入【当前用户可看的】、【已下发的】公告 **/
        systemNoticeService.pullIssuedSystemNotice();

        /**
         * 检查是否存在【当前用户可看的】、【到了生效时间，却还没下发】公告，如果有，则下发给当前用户
         * 其他的通知用户，如果在线则由前端定时拉取，如果离线则登录时拉取
         **/
        systemNoticeService.pullActiveSystemNotice();
    }

}
