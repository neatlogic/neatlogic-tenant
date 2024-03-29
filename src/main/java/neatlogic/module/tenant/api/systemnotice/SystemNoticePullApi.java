/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

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

    @Override
    public String getToken() {
        return "systemnotice/pull";
    }

    @Override
    public String getName() {
        return "nmtas.systemnoticepullapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "issueTime", type = ApiParamType.LONG, desc = "term.framework.issuetime"),
            @Param(name = "direction", type = ApiParamType.ENUM, rule = "before", desc = "nmtas.systemnoticepullapi.input.param.desc.direction"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword", xss=true),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "common.isneedpage")
    })
    @Output({
            @Param(name = "tbodyList", explode = SystemNoticeVo.class, desc = "common.tbodylist"),
            @Param(name = "popUpNoticeIdList",desc = "nmtas.systemnoticepullapi.output.param.desc.popupnoticeidlist"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "nmtas.systemnoticepullapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        SystemNoticeVo vo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<SystemNoticeVo>() {});

        List<String> uuidList = new ArrayList<>();
        AuthenticationInfoVo authenticationInfoVo = UserContext.get().getAuthenticationInfoVo();
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
