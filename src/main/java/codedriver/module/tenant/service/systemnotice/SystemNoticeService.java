package codedriver.module.tenant.service.systemnotice;

import java.util.List;

/**
 * @Title: SystemNoticeService
 * @Package: codedriver.framework.systemnotice.service
 * @Description:
 * @Author: laiwt
 * @Date: 2021/1/20 11:00
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
public interface SystemNoticeService {

    /**
     * @Description: 如果当前用户的system_notice_user中存在system_notice中没有的公告记录，清理掉
     * @Author: laiwt
     * @Date: 2021/1/20 11:10
     * @Params: []
     * @Returns: void
    **/
    public void clearSystemNoticeUser();

    /**
     * @Description: 检查是否存在【已发布却到了失效时间的】公告，如果有，则停用
     * @Author: laiwt
     * @Date: 2021/1/20 11:12
     * @Params: [uuidList]
     * @Returns: void
    **/
    public void stopExpiredSystemNotice(List<String> uuidList);

    /**
     * @Description: 在system_notice_user插入【当前用户可看的】、【已发布的】公告
     * @Author: laiwt
     * @Date: 2021/1/20 11:12
     * @Params: [uuidList]
     * @Returns: void
    **/
    public void pullIssuedSystemNotice(List<String> uuidList);

    /**
     * @Description: 检查是否存在【当前用户可看的】、【到了生效时间，却还没发布】公告，
     * 如果有则下发给当前用户
     * @Author: laiwt
     * @Date: 2021/1/20 11:13
     * @Params: [uuidList]
     * @Returns: void
    **/
    public void pullActiveSystemNotice(List<String> uuidList);
}
