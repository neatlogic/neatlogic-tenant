package codedriver.module.tenant.api.systemnotice;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.UserType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.systemnotice.dao.mapper.SystemNoticeMapper;
import codedriver.framework.systemnotice.dto.SystemNoticeVo;
import codedriver.framework.util.HtmlUtil;
import codedriver.module.tenant.service.systemnotice.SystemNoticeService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Title: SystemNoticePullApi
 * @Package: codedriver.module.tenant.api.systemnotice
 * @Description: 系统公告拉取接口
 * @Author: laiwt
 * @Date: 2021/1/13 18:01
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/

@Service
@OperationType(type = OperationTypeEnum.OPERATE)
public class SystemNoticePullApi extends PrivateApiComponentBase {

    @Autowired
    private SystemNoticeService systemNoticeService;

    @Autowired
    private SystemNoticeMapper systemNoticeMapper;

    @Autowired
    private UserMapper userMapper;

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
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字", xss=true),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数据条目"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true")
    })
    @Output({
            @Param(name = "tbodyList", explode = SystemNoticeVo.class, desc = "公告列表"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "拉取系统公告")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        SystemNoticeVo vo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<SystemNoticeVo>() {});

        List<String> uuidList = new ArrayList<>();
        uuidList.add(UserContext.get().getUserUuid(true));
        uuidList.add(UserType.ALL.getValue());
        uuidList.addAll(userMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true)));
        uuidList.addAll(userMapper.getRoleUuidListByUserUuid(UserContext.get().getUserUuid(true)));

        systemNoticeService.clearSystemNoticeUser();
        systemNoticeService.stopExpiredSystemNotice();
        systemNoticeService.pullIssuedSystemNotice();
        systemNoticeService.pullActiveSystemNotice();

        vo.setRecipientList(uuidList);
        JSONObject returnObj = new JSONObject();
        if (vo.getNeedPage()) {
            int rowNum = systemNoticeMapper.searchIssuedNoticeCountByUserUuid(UserContext.get().getUserUuid(true));
            returnObj.put("pageSize", vo.getPageSize());
            returnObj.put("currentPage", vo.getCurrentPage());
            returnObj.put("rowNum", rowNum);
            returnObj.put("pageCount", PageUtil.getPageCount(rowNum, vo.getPageSize()));
        }
        List<SystemNoticeVo> noticeList = systemNoticeMapper.searchIssuedNoticeListByUserUuid(vo,UserContext.get().getUserUuid(true));
        if(CollectionUtils.isNotEmpty(noticeList)){
            noticeList.stream().forEach(o -> o.setContent(HtmlUtil.removeHtml(o.getContent(),null)));
        }
        returnObj.put("tbodyList",noticeList);

        // todo 更换逻辑 --laiwt
        /** 按发布时间倒序，寻找第一个需要弹窗的公告 **/
//        SystemNoticeVo popUpNotice = systemNoticeMapper.getFirstPopUpNoticeByUserUuid(UserContext.get().getUserUuid(true),null);
//        if(popUpNotice != null){
//            /** 更新状态为已读 **/
//            systemNoticeMapper.updateSystemNoticeUserReadStatus(popUpNotice.getId(),UserContext.get().getUserUuid(true));
//            /** 判断是否有下一条需要弹窗的公告 **/
//            int hasNext = systemNoticeMapper.checkHasNextNeedPopUpNoticeByUserUuid(UserContext.get().getUserUuid(true),popUpNotice.getId());
//            returnObj.put("popUpNotice",popUpNotice);
//            returnObj.put("hasNext",hasNext);
//        }

        return returnObj;
    }
}
