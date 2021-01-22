//package codedriver.module.tenant.api.systemnotice;
//
//import codedriver.framework.asynchronization.threadlocal.UserContext;
//import codedriver.framework.common.constvalue.ApiParamType;
//import codedriver.framework.restful.annotation.*;
//import codedriver.framework.restful.constvalue.OperationTypeEnum;
//import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
//import codedriver.framework.systemnotice.dao.mapper.SystemNoticeMapper;
//import codedriver.framework.systemnotice.dto.SystemNoticeVo;
//import com.alibaba.fastjson.JSONObject;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
///**
// * @Title: NextPopUpSystemNoticeGetApi
// * @Package: codedriver.module.tenant.api.systemnotice
// * @Description: 获取下一个需要弹窗的公告
// * @Author: laiwt
// * @Date: 2021/1/13 18:01
// * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
// * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
// **/
//
//@Service
//@OperationType(type = OperationTypeEnum.OPERATE)
//public class NextPopUpSystemNoticeGetApi extends PrivateApiComponentBase {
//
//    @Autowired
//    private SystemNoticeMapper systemNoticeMapper;
//
//    @Override
//    public String getToken() {
//        return "systemnotice/nextpopup/get";
//    }
//
//    @Override
//    public String getName() {
//        return "获取下一个需要弹窗的公告";
//    }
//
//    @Override
//    public String getConfig() {
//        return null;
//    }
//
//    @Input({@Param(name = "currentId", type = ApiParamType.LONG, isRequired = true, desc = "当前公告ID")})
//    @Output({
//            @Param(name = "popUpNotice", explode = SystemNoticeVo.class, desc = "需要弹窗的公告"),
//            @Param(name = "hasNext", desc = "是否有下一条需要弹窗的公告(1:是;0:否)")
//    })
//    @Description(desc = "获取下一个需要弹窗的公告")
//    @Override
//    public Object myDoService(JSONObject jsonObj) throws Exception {
//        JSONObject returnObj = new JSONObject();
//        /** 按发布时间倒序，寻找【除了当前公告外】第一个需要弹窗的公告 **/
//        SystemNoticeVo popUpNotice = systemNoticeMapper.getFirstPopUpNoticeByUserUuid(UserContext.get().getUserUuid(true),jsonObj.getLong("currentId"));
//        if(popUpNotice != null){
//            /** 更新状态为已读 **/
//            systemNoticeMapper.updateSystemNoticeUserReadStatus(popUpNotice.getId(),UserContext.get().getUserUuid(true));
//            /** 判断是否有下一条需要弹窗的公告 **/
//            int hasNext = systemNoticeMapper.checkHasNextNeedPopUpNoticeByUserUuid(UserContext.get().getUserUuid(true),popUpNotice.getId());
//            returnObj.put("popUpNotice",popUpNotice);
//            returnObj.put("hasNext",hasNext);
//        }
//
//        return returnObj;
//    }
//}
