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

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.common.constvalue.UserType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.RoleVo;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.systemnotice.dao.mapper.SystemNoticeMapper;
import neatlogic.framework.systemnotice.dto.SystemNoticeRecipientVo;
import neatlogic.framework.systemnotice.dto.SystemNoticeVo;
import neatlogic.framework.util.HtmlUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class SystemNoticeSearchApi extends PrivateApiComponentBase {

    @Autowired
    private SystemNoticeMapper systemNoticeMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public String getToken() {
        return "systemnotice/search";
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
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字", xss=true),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数据条目"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true")
    })
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, explode = SystemNoticeVo.class),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "查询系统公告")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        SystemNoticeVo vo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<SystemNoticeVo>() {});
        JSONObject returnObj = new JSONObject();
        if (vo.getNeedPage()) {
            int rowNum = systemNoticeMapper.searchSystemNoticeCount(vo);
            returnObj.put("pageSize", vo.getPageSize());
            returnObj.put("currentPage", vo.getCurrentPage());
            returnObj.put("rowNum", rowNum);
            returnObj.put("pageCount", PageUtil.getPageCount(rowNum, vo.getPageSize()));
        }
        List<SystemNoticeVo> noticeVoList = systemNoticeMapper.searchSystemNotice(vo);
        if(CollectionUtils.isNotEmpty(noticeVoList)){
            for(SystemNoticeVo noticeVo : noticeVoList){
                List<SystemNoticeRecipientVo> recipientVoList = systemNoticeMapper.getRecipientListByNoticeId(noticeVo.getId());
                if(CollectionUtils.isNotEmpty(recipientVoList)){
                    List<Object> recipientObjList = new ArrayList<>();
                    for(SystemNoticeRecipientVo recipientVo : recipientVoList){
                        if(GroupSearch.USER.getValue().equals(recipientVo.getType())){
                            UserVo user = userMapper.getUserBaseInfoByUuidWithoutCache(recipientVo.getUuid());
                            if(user != null){
                                recipientObjList.add(user);
                            }
                        }else if(GroupSearch.TEAM.getValue().equals(recipientVo.getType())){
                            TeamVo team = teamMapper.getTeamByUuid(recipientVo.getUuid());
                            if(team != null){
                                recipientObjList.add(team);
                            }
                        }else if(GroupSearch.ROLE.getValue().equals(recipientVo.getType())){
                            RoleVo role = roleMapper.getRoleByUuid(recipientVo.getUuid());
                            if(role != null){
                                recipientObjList.add(role);
                            }
                        }else if(GroupSearch.COMMON.getValue().equals(recipientVo.getType())){
                            JSONObject obj = new JSONObject();
                            obj.put("initType",recipientVo.getType());
                            obj.put("name", UserType.getText(recipientVo.getUuid()));
                            recipientObjList.add(obj);
                        }
                    }
                    noticeVo.setRecipientObjList(recipientObjList);
                }
                /** 提取内容中的图片 **/
                noticeVo.setImgList(HtmlUtil.getImgSrcList(noticeVo.getContent()));
                /** 过滤掉内容中所有的HTML标签 **/
                noticeVo.setContent(HtmlUtil.removeHtml(noticeVo.getContent(),null));
                noticeVo.setStatusVo(SystemNoticeVo.Status.getStatus(noticeVo.getStatus()));
            }
        }
        returnObj.put("tbodyList",noticeVoList);
        return returnObj;
    }
}
