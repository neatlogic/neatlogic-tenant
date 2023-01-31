package neatlogic.module.tenant.service.systemnotice;


/**
 * @Title: SystemNoticeService
 * @Package: neatlogic.framework.systemnotice.service
 * @Description:
 * @Author: laiwt
 * @Date: 2021/1/20 11:00
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
public interface SystemNoticeService {

    /**
     * @Description: 清理掉system_notice_user中因删除公告或更改公告通知对象而遗留的记录
     * @Author: laiwt
     * @Date: 2021/1/20 11:10
     * @Params: []
     * @Returns: void
    **/
    public void clearSystemNoticeUser();

    /**
     * @Description: 检查是否存在【已下发却到了失效时间的】公告，如果有，则停用
     * @Author: laiwt
     * @Date: 2021/1/20 11:12
     * @Params: []
     * @Returns: void
    **/
    public void stopExpiredSystemNotice();

    /**
     * @Description: 在system_notice_user插入【当前用户可看的】、【已下发的】公告
     * @Author: laiwt
     * @Date: 2021/1/20 11:12
     * @Params: []
     * @Returns: void
    **/
    public void pullIssuedSystemNotice();

    /**
     * @Description: 检查是否存在【当前用户可看的】、【到了生效时间，却还没下发】公告，
     * 如果有则下发给当前用户
     * @Author: laiwt
     * @Date: 2021/1/20 11:13
     * @Params: []
     * @Returns: void
    **/
    public void pullActiveSystemNotice();
}
