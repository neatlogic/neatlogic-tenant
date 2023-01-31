/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.tenant.api.systemnotice;

import neatlogic.framework.asynchronization.thread.CodeDriverThread;
import neatlogic.framework.asynchronization.threadpool.CachedThreadPool;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.common.constvalue.UserType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dao.mapper.UserSessionMapper;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.systemnotice.dao.mapper.SystemNoticeMapper;
import neatlogic.framework.systemnotice.dto.SystemNoticeRecipientVo;
import neatlogic.framework.systemnotice.dto.SystemNoticeUserVo;
import neatlogic.framework.systemnotice.dto.SystemNoticeVo;
import neatlogic.framework.systemnotice.exception.SystemNoticeExpiredTimeLessThanActiveTimeException;
import neatlogic.framework.systemnotice.exception.SystemNoticeHasBeenIssuedException;
import neatlogic.framework.systemnotice.exception.SystemNoticeNotFoundException;
import neatlogic.framework.auth.label.SYSTEM_NOTICE_MODIFY;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@AuthAction(action = SYSTEM_NOTICE_MODIFY.class)
@Service
@OperationType(type = OperationTypeEnum.OPERATE)
@Transactional
public class SystemNoticeIssueApi extends PrivateApiComponentBase {

    private final static Integer PAGE_SIZE = 100;

    @Resource
    private SystemNoticeMapper systemNoticeMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserSessionMapper userSessionMapper;

    @Override
    public String getToken() {
        return "systemnotice/issue";
    }

    @Override
    public String getName() {
        return "下发系统公告";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "公告ID"),
            @Param(name = "startTime", type = ApiParamType.LONG, desc = "生效时间"),
            @Param(name = "endTime", type = ApiParamType.LONG, desc = "失效时间"),
            @Param(name = "popUp", type = ApiParamType.ENUM, rule = "longshow,close", desc = "是否弹窗(longshow:持续弹窗;close:不弹窗)", isRequired = true),
            @Param(name = "ignoreRead", type = ApiParamType.ENUM, rule = "0,1", desc = "1:忽略已读;0:不忽略已读")
    })
    @Output({})
    @Description(desc = "下发系统公告")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        SystemNoticeVo vo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<SystemNoticeVo>() {
        });
        SystemNoticeVo oldVo = systemNoticeMapper.getSystemNoticeBaseInfoById(vo.getId());
        if (oldVo == null) {
            throw new SystemNoticeNotFoundException(vo.getId());
        }
        if (SystemNoticeVo.Status.ISSUED.getValue().equals(oldVo.getStatus())) {
            throw new SystemNoticeHasBeenIssuedException(oldVo.getTitle());
        }
        if (vo.getStartTime() != null && vo.getEndTime() != null && vo.getEndTime().getTime() < vo.getStartTime().getTime()) {
            throw new SystemNoticeExpiredTimeLessThanActiveTimeException();
        }

        long currentTimeMillis = System.currentTimeMillis();
        /*
          符合以下情形之一则立即下发给通知范围内的在线用户
          1、没有设置生效与失效时间
          2、没有设置生效时间且失效时间大于当前时间
          3、生效时间小于等于当前时间且失效时间大于当前时间
          4、生效时间小于等于当前时间且没有设置失效时间
        */
        if ((vo.getStartTime() == null || (vo.getStartTime() != null && currentTimeMillis >= vo.getStartTime().getTime()))
                && (vo.getEndTime() == null || (vo.getEndTime() != null && currentTimeMillis < vo.getEndTime().getTime()))) {

            /* 立即更改公告状态为已下发 **/
            vo.setStatus(SystemNoticeVo.Status.ISSUED.getValue());
            vo.setIssueTime(vo.getStartTime() == null ? new Date() : vo.getStartTime());

            /* 如果没有忽略已读，则把system_notice_user中的is_read设为0 **/
            if (vo.getIgnoreRead() != null && vo.getIgnoreRead() == 0) {
                /* 经测试，该语句update 53万条数据耗时约1.2s，故不单独开线程执行 **/
                systemNoticeMapper.updateReadStatusToNotReadByNoticeId(vo.getId());
            }

            List<SystemNoticeRecipientVo> recipientList = systemNoticeMapper.getRecipientListByNoticeId(vo.getId());
            if (CollectionUtils.isNotEmpty(recipientList)) {
                long expireTime = currentTimeMillis - TimeUnit.MINUTES.toMillis(Config.USER_EXPIRETIME());
                if (recipientList.stream().anyMatch(o -> UserType.ALL.getValue().equals(o.getUuid()))) {
                    /* 如果通知范围是所有人，那么找出当前所有的在线用户 **/
                    int allOnlineUserCount = userSessionMapper.getAllOnlineUserCount(new Date(expireTime));
                    if (allOnlineUserCount > 0) {
                        CachedThreadPool.execute(new CodeDriverThread("NOTICE-INSERTER") {
                            @Override
                            protected void execute() {
                                Date expireDate = new Date(expireTime);
                                BasePageVo pageVo = new BasePageVo();
                                pageVo.setPageSize(PAGE_SIZE);
                                pageVo.setPageCount(PageUtil.getPageCount(allOnlineUserCount, pageVo.getPageSize()));
                                List<SystemNoticeUserVo> noticeUserVoList = new ArrayList<>();
                                for (int i = 1; i <= pageVo.getPageCount(); i++) {
                                    pageVo.setCurrentPage(i);
                                    List<String> allOnlineUser = userSessionMapper.getAllOnlineUser(expireDate, pageVo.getStartNum(), pageVo.getPageSize());
                                    if (CollectionUtils.isNotEmpty(allOnlineUser)) {
                                        allOnlineUser.forEach(o -> noticeUserVoList.add(new SystemNoticeUserVo(vo.getId(), o)));
                                        systemNoticeMapper.batchInsertSystemNoticeUser(noticeUserVoList);
                                        noticeUserVoList.clear();
                                    }
                                }
                            }
                        });
                    }
                } else {
                    List<String> userUuidList = recipientList.stream()
                            .filter(o -> GroupSearch.USER.getValue().equals(o.getType()))
                            .map(SystemNoticeRecipientVo::getUuid)
                            .collect(Collectors.toList());
                    List<String> teamUuidList = recipientList.stream()
                            .filter(o -> GroupSearch.TEAM.getValue().equals(o.getType()))
                            .map(SystemNoticeRecipientVo::getUuid)
                            .collect(Collectors.toList());
                    List<String> roleUuidList = recipientList.stream()
                            .filter(o -> GroupSearch.ROLE.getValue().equals(o.getType()))
                            .map(SystemNoticeRecipientVo::getUuid)
                            .collect(Collectors.toList());
                    int onlineUserCount = userSessionMapper.getOnlineUserUuidListByUserUuidListAndTeamUuidListAndRoleUuidListAndGreaterThanSessionTimeCount
                            (userUuidList, teamUuidList, roleUuidList, new Date(expireTime));
                    if (onlineUserCount > 0) {
                        CachedThreadPool.execute(new CodeDriverThread("NOTICE-INSERTER") {
                            @Override
                            protected void execute() {
                                Date expireDate = new Date(expireTime);
                                int count = onlineUserCount / PAGE_SIZE + 1;
                                List<SystemNoticeUserVo> noticeUserVoList = new ArrayList<>();
                                for (int i = 0; i < count; i++) {
                                    List<String> onlineUserList = userSessionMapper.getOnlineUserUuidListByUserUuidListAndTeamUuidListAndRoleUuidListAndGreaterThanSessionTime
                                            (userUuidList, teamUuidList, roleUuidList, expireDate, true, i, PAGE_SIZE);
                                    if (CollectionUtils.isNotEmpty(onlineUserList)) {
                                        onlineUserList.forEach(o -> noticeUserVoList.add(new SystemNoticeUserVo(vo.getId(), o)));
                                        systemNoticeMapper.batchInsertSystemNoticeUser(noticeUserVoList);
                                        noticeUserVoList.clear();
                                    }
                                }
                            }
                        });
                    }
                }
            }
        }
        systemNoticeMapper.updateSystemNoticeIssueInfo(vo);

        return null;
    }
}
