package codedriver.module.tenant.api.systemnotice;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.systemnotice.dao.mapper.SystemNoticeMapper;
import codedriver.framework.systemnotice.dto.SystemNoticeVo;
import codedriver.framework.systemnotice.exception.SystemNoticeHasBeenIssuedException;
import codedriver.framework.systemnotice.exception.SystemNoticeNotFoundException;
import codedriver.module.tenant.auth.label.SYSTEM_NOTICE_MODIFY;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Title: SystemNoticeDeleteApi
 * @Package: codedriver.module.tenant.api.systemnotice
 * @Description: 系统公告删除接口
 * @Author: laiwt
 * @Date: 2021/1/13 18:01
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/

@AuthAction(action = SYSTEM_NOTICE_MODIFY.class)
@Service
@OperationType(type = OperationTypeEnum.DELETE)
@Transactional
public class SystemNoticeDeleteApi extends PrivateApiComponentBase {

    @Autowired
    private SystemNoticeMapper systemNoticeMapper;

    @Override
    public String getToken() {
        return "systemnotice/delete";
    }

    @Override
    public String getName() {
        return "删除系统公告";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "公告ID")})
    @Output({})
    @Description(desc = "删除系统公告")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        SystemNoticeVo vo = systemNoticeMapper.getSystemNoticeBaseInfoById(jsonObj.getLong("id"));
        if(vo == null){
            throw new SystemNoticeNotFoundException(jsonObj.getLong("id"));
        }
        if(SystemNoticeVo.Status.ISSUED.getValue().equals(vo.getStatus())){
            throw new SystemNoticeHasBeenIssuedException(vo.getTitle());
        }
        /** 只删除system_notice与system_notice_recipient，
         * system_notice_user由每个用户登录或者pull时自我删除
         **/
        systemNoticeMapper.deleteRecipientByNoticeId(vo.getId());
        systemNoticeMapper.deleteSystemNoticeById(vo.getId());
        return null;
    }
}