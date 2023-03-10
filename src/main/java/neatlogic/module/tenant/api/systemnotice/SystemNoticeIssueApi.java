/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.systemnotice;

import neatlogic.framework.asynchronization.thread.NeatLogicThread;
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
        return "??????????????????";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "??????ID"),
            @Param(name = "startTime", type = ApiParamType.LONG, desc = "????????????"),
            @Param(name = "endTime", type = ApiParamType.LONG, desc = "????????????"),
            @Param(name = "popUp", type = ApiParamType.ENUM, rule = "longshow,close", desc = "????????????(longshow:????????????;close:?????????)", isRequired = true),
            @Param(name = "ignoreRead", type = ApiParamType.ENUM, rule = "0,1", desc = "1:????????????;0:???????????????")
    })
    @Output({})
    @Description(desc = "??????????????????")
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
          ????????????????????????????????????????????????????????????????????????
          1????????????????????????????????????
          2????????????????????????????????????????????????????????????
          3????????????????????????????????????????????????????????????????????????
          4??????????????????????????????????????????????????????????????????
        */
        if ((vo.getStartTime() == null || (vo.getStartTime() != null && currentTimeMillis >= vo.getStartTime().getTime()))
                && (vo.getEndTime() == null || (vo.getEndTime() != null && currentTimeMillis < vo.getEndTime().getTime()))) {

            /* ???????????????????????????????????? **/
            vo.setStatus(SystemNoticeVo.Status.ISSUED.getValue());
            vo.setIssueTime(vo.getStartTime() == null ? new Date() : vo.getStartTime());

            /* ?????????????????????????????????system_notice_user??????is_read??????0 **/
            if (vo.getIgnoreRead() != null && vo.getIgnoreRead() == 0) {
                /* ?????????????????????update 53?????????????????????1.2s?????????????????????????????? **/
                systemNoticeMapper.updateReadStatusToNotReadByNoticeId(vo.getId());
            }

            List<SystemNoticeRecipientVo> recipientList = systemNoticeMapper.getRecipientListByNoticeId(vo.getId());
            if (CollectionUtils.isNotEmpty(recipientList)) {
                long expireTime = currentTimeMillis - TimeUnit.MINUTES.toMillis(Config.USER_EXPIRETIME());
                if (recipientList.stream().anyMatch(o -> UserType.ALL.getValue().equals(o.getUuid()))) {
                    /* ???????????????????????????????????????????????????????????????????????? **/
                    int allOnlineUserCount = userSessionMapper.getAllOnlineUserCount(new Date(expireTime));
                    if (allOnlineUserCount > 0) {
                        CachedThreadPool.execute(new NeatLogicThread("NOTICE-INSERTER") {
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
                        CachedThreadPool.execute(new NeatLogicThread("NOTICE-INSERTER") {
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
