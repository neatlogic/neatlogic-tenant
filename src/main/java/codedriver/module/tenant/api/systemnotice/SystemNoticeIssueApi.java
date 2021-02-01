package codedriver.module.tenant.api.systemnotice;

import codedriver.framework.asynchronization.thread.CodeDriverThread;
import codedriver.framework.asynchronization.threadpool.CommonThreadPool;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.config.Config;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.common.constvalue.UserType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.systemnotice.dao.mapper.SystemNoticeMapper;
import codedriver.framework.systemnotice.dto.SystemNoticeRecipientVo;
import codedriver.framework.systemnotice.dto.SystemNoticeUserVo;
import codedriver.framework.systemnotice.dto.SystemNoticeVo;
import codedriver.framework.systemnotice.exception.SystemNoticeHasBeenIssuedException;
import codedriver.framework.systemnotice.exception.SystemNoticeExpiredTimeLessThanActiveTimeException;
import codedriver.framework.systemnotice.exception.SystemNoticeNotFoundException;
import codedriver.module.tenant.auth.label.SYSTEM_NOTICE_MODIFY;
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

/**
 * @Title: SystemNoticeIssueApi
 * @Package: codedriver.module.tenant.api.systemnotice
 * @Description: 系统公告下发接口
 * @Author: laiwt
 * @Date: 2021/1/13 18:01
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/

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
            @Param(name = "popUp", type = ApiParamType.ENUM, rule = "longshow,close", desc = "是否弹窗(longshow:持续弹窗;close:不弹窗)"),
            @Param(name = "ignoreRead", type = ApiParamType.ENUM, rule = "0,1", desc = "1:忽略已读;0:不忽略已读")
    })
    @Output({})
    @Description(desc = "下发系统公告")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        SystemNoticeVo vo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<SystemNoticeVo>() {});
        SystemNoticeVo oldVo = systemNoticeMapper.getSystemNoticeBaseInfoById(vo.getId());
        if (oldVo == null) {
            throw new SystemNoticeNotFoundException(vo.getId());
        }
        if (SystemNoticeVo.Status.ISSUED.getValue().equals(oldVo.getStatus())) {
            throw new SystemNoticeHasBeenIssuedException(oldVo.getTitle());
        }
        if(vo.getStartTime() != null && vo.getEndTime() != null && vo.getEndTime().getTime() < vo.getStartTime().getTime()){
            throw new SystemNoticeExpiredTimeLessThanActiveTimeException();
        }

        long currentTimeMillis = System.currentTimeMillis();
        /**
         * 符合以下情形之一则立即下发给通知范围内的在线用户
         * 1、没有设置生效与失效时间
         * 2、没有设置生效时间且失效时间大于当前时间
         * 3、生效时间小于等于当前时间且失效时间大于当前时间
         * 4、生效时间小于等于当前时间且没有设置失效时间
        **/
        if((vo.getStartTime() == null || (vo.getStartTime() != null && currentTimeMillis >= vo.getStartTime().getTime()))
                && (vo.getEndTime() == null || (vo.getEndTime() != null && currentTimeMillis < vo.getEndTime().getTime()))){

            /** 立即更改公告状态为已发布 **/
            vo.setStatus(SystemNoticeVo.Status.ISSUED.getValue());
            vo.setIssueTime(vo.getStartTime() == null ? new Date() : vo.getStartTime());

            List<SystemNoticeRecipientVo> recipientList = systemNoticeMapper.getRecipientListByNoticeId(vo.getId());
            if (CollectionUtils.isNotEmpty(recipientList)) {
                long expireTime = currentTimeMillis - TimeUnit.MINUTES.toMillis(Config.USER_EXPIRETIME());
                if (recipientList.stream().anyMatch(o -> UserType.ALL.getValue().equals(o.getUuid()))) {
                    /** 如果通知范围是所有人，那么找出当前所有的在线用户 **/
                    int allOnlineUserCount = userMapper.getAllOnlineUserCount(new Date(expireTime));
                    if(allOnlineUserCount > 0){
                        CommonThreadPool.execute(new CodeDriverThread() {
                            @Override
                            protected void execute() {
                                Date expireDate = new Date(expireTime);
                                int count = allOnlineUserCount / PAGE_SIZE.intValue() + 1;
                                List<SystemNoticeUserVo> noticeUserVoList = new ArrayList<>();
                                for (int i = 0; i < count; i++) {
                                    List<String> allOnlineUser = userMapper.getAllOnlineUser(expireDate, i, PAGE_SIZE);
                                    if(CollectionUtils.isNotEmpty(allOnlineUser)){
                                        allOnlineUser.stream().forEach(o -> noticeUserVoList.add(new SystemNoticeUserVo(vo.getId(), o)));
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
                    int onlineUserCount = userMapper.getOnlineUserUuidListByUserUuidListAndTeamUuidListAndRoleUuidListAndGreaterThanSessionTimeCount
                                    (userUuidList, teamUuidList, roleUuidList, new Date(expireTime));
                    if(onlineUserCount > 0){
                        CommonThreadPool.execute(new CodeDriverThread() {
                            @Override
                            protected void execute() {
                                Date expireDate = new Date(expireTime);
                                int count = onlineUserCount / PAGE_SIZE.intValue() + 1;
                                List<SystemNoticeUserVo> noticeUserVoList = new ArrayList<>();
                                for (int i = 0; i < count; i++) {
                                    List<String> onlineUserList = userMapper.getOnlineUserUuidListByUserUuidListAndTeamUuidListAndRoleUuidListAndGreaterThanSessionTime
                                            (userUuidList, teamUuidList, roleUuidList, expireDate,true,i,PAGE_SIZE);
                                    if(CollectionUtils.isNotEmpty(onlineUserList)){
                                        onlineUserList.stream().forEach(o -> noticeUserVoList.add(new SystemNoticeUserVo(vo.getId(), o)));
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
