package codedriver.module.tenant.service.systemnotice;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.UserType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.login.core.LoginPostProcessorBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Title: LoginPullSystemNoticeProcessor
 * @Package codedriver.framework.message.login.handler
 * @Description: 登录后拉取系统公告处理器
 * @Author: laiwt
 * @Date: 2021/1/15 15:38
 * Copyright(c) 2020 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
@Service
public class LoginPullSystemNoticeProcessor extends LoginPostProcessorBase {

    @Autowired
    private SystemNoticeService systemNoticeService;

    @Autowired
    private UserMapper userMapper;


    @Override
    protected void myLoginAfterInitialization() {

        List<String> uuidList = new ArrayList<>();
        uuidList.add(UserContext.get().getUserUuid(true));
        uuidList.add(UserType.ALL.getValue());
        uuidList.addAll(userMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true)));
        uuidList.addAll(userMapper.getRoleUuidListByUserUuid(UserContext.get().getUserUuid(true)));

        /** 清理system_notice_user中【不存在的公告】记录 **/
        systemNoticeService.clearSystemNoticeUser();

        /** 检查是否存在【已发布却到了失效时间的】公告，如果有，则停用 **/
        systemNoticeService.stopExpiredSystemNotice(uuidList);

        /** 在system_notice_user插入【当前用户可看的】、【已发布的】公告 **/
        systemNoticeService.pullIssuedSystemNotice(uuidList);

        /**
         * 检查是否存在【当前用户可看的】、【到了生效时间，却还没发布】公告，如果有，则下发给自己
         * 其他的通知用户，如果在线则由前端定时拉取，如果离线则登录时拉取
         **/
        systemNoticeService.pullActiveSystemNotice(uuidList);
    }

}
