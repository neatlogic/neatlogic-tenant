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
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.systemnotice.dao.mapper.SystemNoticeMapper;
import neatlogic.framework.systemnotice.dto.SystemNoticeVo;
import neatlogic.framework.util.HtmlUtil;
import neatlogic.framework.util.TimeUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class SystemNoticeHistoryListApi extends PrivateApiComponentBase {

    @Autowired
    private SystemNoticeMapper systemNoticeMapper;

    @Override
    public String getToken() {
        return "systemnotice/history/list";
    }

    @Override
    public String getName() {
        return "查询系统公告";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "startTime", type = ApiParamType.LONG, desc = "开始时间"),
            @Param(name = "endTime", type = ApiParamType.LONG, desc = "结束时间"),
            @Param(name = "timeRange", type = ApiParamType.INTEGER, desc = "时间跨度"),
            @Param(name = "timeUnit", type = ApiParamType.ENUM, rule = "year,month,week,day,hour", desc = "时间跨度单位"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字", xss=true),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数据条目"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true")
    })
    @Output({
            @Param(name = "notReadCount", type = ApiParamType.INTEGER, desc = "未读的公告数"),
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, explode = SystemNoticeVo.class),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "查询系统公告")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        SystemNoticeVo vo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<SystemNoticeVo>() {});
        if(vo.getStartTime() == null && vo.getEndTime() == null){
            Integer timeRange = jsonObj.getInteger("timeRange");
            String timeUnit = jsonObj.getString("timeUnit");
            if(timeRange != null && StringUtils.isNotBlank(timeUnit)){
                vo.setStartTime(TimeUtil.recentTimeTransfer(timeRange, timeUnit));
                vo.setEndTime(new Date());
            }
        }

        if (vo.getNeedPage()) {
            int rowNum = systemNoticeMapper.searchNoticeHistoryCountByUserUuid(UserContext.get().getUserUuid(true),vo);
            returnObj.put("pageSize", vo.getPageSize());
            returnObj.put("currentPage", vo.getCurrentPage());
            returnObj.put("rowNum", rowNum);
            returnObj.put("pageCount", PageUtil.getPageCount(rowNum, vo.getPageSize()));
        }
        List<SystemNoticeVo> noticeVoList = systemNoticeMapper.searchNoticeHistoryListByUserUuid(vo,UserContext.get().getUserUuid(true));
        if(CollectionUtils.isNotEmpty(noticeVoList)){
            for(SystemNoticeVo noticeVo : noticeVoList){
                /** 提取内容中的图片 **/
                noticeVo.setImgList(HtmlUtil.getImgSrcList(noticeVo.getContent()));
                /** 过滤掉内容中所有的HTML标签 **/
                noticeVo.setContent(HtmlUtil.removeHtml(noticeVo.getContent(),null));
            }
        }
        /** 计算未读的公告数量 **/
        vo.setIsRead(0);
        int notReadCount = systemNoticeMapper.searchNoticeHistoryCountByUserUuid(UserContext.get().getUserUuid(),vo);
        returnObj.put("notReadCount",notReadCount);
        returnObj.put("tbodyList",noticeVoList);
        return returnObj;
    }
}
