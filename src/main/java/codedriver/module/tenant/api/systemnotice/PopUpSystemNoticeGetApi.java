package codedriver.module.tenant.api.systemnotice;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.systemnotice.dao.mapper.SystemNoticeMapper;
import codedriver.framework.systemnotice.dto.SystemNoticeVo;
import codedriver.framework.systemnotice.exception.SystemNoticeNotFoundException;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Title: PopUpSystemNoticeGetApi
 * @Package: codedriver.module.tenant.api.systemnotice
 * @Description: 获取需要弹窗的公告
 * @Author: laiwt
 * @Date: 2021/1/13 18:01
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/

@Service
@OperationType(type = OperationTypeEnum.OPERATE)
public class PopUpSystemNoticeGetApi extends PrivateApiComponentBase {

    @Autowired
    private SystemNoticeMapper systemNoticeMapper;

    @Override
    public String getToken() {
        return "systemnotice/popupnotice/get";
    }

    @Override
    public String getName() {
        return "获取需要弹窗的公告";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "公告ID")})
    @Output({@Param(explode = SystemNoticeVo.class, desc = "需要弹窗的公告")})
    @Description(desc = "获取需要弹窗的公告")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        SystemNoticeVo notice = systemNoticeMapper.getSystemNoticeBaseInfoById(id);
        if(notice == null){
            throw new SystemNoticeNotFoundException(id);
        }
//        systemNoticeMapper.updateSystemNoticeUserReadStatus(id,UserContext.get().getUserUuid(true));
        return notice;
    }
}
