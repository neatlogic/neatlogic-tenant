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

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.UserType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.service.AuthenticationInfoService;
import neatlogic.framework.systemnotice.dao.mapper.SystemNoticeMapper;
import neatlogic.framework.systemnotice.dto.SystemNoticeVo;
import neatlogic.framework.util.HtmlUtil;
import neatlogic.module.tenant.service.systemnotice.SystemNoticeService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service

@OperationType(type = OperationTypeEnum.OPERATE)
public class SystemNoticePullApi extends PrivateApiComponentBase {

    @Resource
    private SystemNoticeService systemNoticeService;

    @Resource
    private SystemNoticeMapper systemNoticeMapper;

    @Resource
    private AuthenticationInfoService authenticationInfoService;

    @Override
    public String getToken() {
        return "systemnotice/pull";
    }

    @Override
    public String getName() {
        return "拉取系统公告";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "issueTime", type = ApiParamType.LONG, desc = "下发时间"),
            @Param(name = "direction", type = ApiParamType.ENUM, rule = "before", desc = "before:找issueTime之前的公告"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字", xss=true),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数据条目"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true")
    })
    @Output({
            @Param(name = "tbodyList", explode = SystemNoticeVo.class, desc = "公告列表"),
            @Param(name = "popUpNoticeIdList",desc = "需要弹窗的公告ID列表"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "拉取系统公告")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        SystemNoticeVo vo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<SystemNoticeVo>() {});

        List<String> uuidList = new ArrayList<>();
        AuthenticationInfoVo authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(UserContext.get().getUserUuid(true));
        uuidList.add(UserContext.get().getUserUuid(true));
        uuidList.add(UserType.ALL.getValue());
        uuidList.addAll(authenticationInfoVo.getTeamUuidList());
        uuidList.addAll(authenticationInfoVo.getRoleUuidList());

        systemNoticeService.clearSystemNoticeUser();
        systemNoticeService.stopExpiredSystemNotice();
        systemNoticeService.pullIssuedSystemNotice();
        systemNoticeService.pullActiveSystemNotice();

        vo.setRecipientList(uuidList);
        vo.setIsRead(0);
        JSONObject returnObj = new JSONObject();
        if (vo.getNeedPage()) {
            int rowNum = systemNoticeMapper.searchIssuedNoticeCountByUserUuid(UserContext.get().getUserUuid(true),vo);
            returnObj.put("pageSize", vo.getPageSize());
            returnObj.put("currentPage", vo.getCurrentPage());
            returnObj.put("rowNum", rowNum);
            returnObj.put("pageCount", PageUtil.getPageCount(rowNum, vo.getPageSize()));
        }
        List<SystemNoticeVo> noticeList = systemNoticeMapper.searchIssuedNoticeListByUserUuid(vo,UserContext.get().getUserUuid(true));
        if(CollectionUtils.isNotEmpty(noticeList)){
            /** 提取内容中的图片&过滤掉所有的HTML标签 **/
            noticeList.stream().forEach(o -> {
                o.setImgList(HtmlUtil.getImgSrcList(o.getContent()));
                o.setContent(HtmlUtil.removeHtml(o.getContent(),null));
            });
        }
        returnObj.put("tbodyList",noticeList);

        /** 查找需要弹窗的公告ID **/
        int popUpNoticeCount = systemNoticeMapper.getPopUpNoticeCountByUserUuid(UserContext.get().getUserUuid(true));
        if(popUpNoticeCount > 0){
            BasePageVo pageVo = new BasePageVo();
            pageVo.setPageSize(100);
            pageVo.setPageCount(PageUtil.getPageCount(popUpNoticeCount, pageVo.getPageSize()));
            List<Long> idList = new ArrayList<>();
            for(int i = 1;i <= pageVo.getPageCount();i++){
                pageVo.setCurrentPage(i);
                List<SystemNoticeVo> popUpNoticeIdList = systemNoticeMapper.getPopUpNoticeIdListByUserUuid(UserContext.get().getUserUuid(true), pageVo);
                if(CollectionUtils.isNotEmpty(popUpNoticeIdList)){
                    idList.addAll(popUpNoticeIdList.stream().map(SystemNoticeVo::getId).collect(Collectors.toList()));
                }
            }
            returnObj.put("popUpNoticeIdList",idList);
        }

        return returnObj;
    }
}
