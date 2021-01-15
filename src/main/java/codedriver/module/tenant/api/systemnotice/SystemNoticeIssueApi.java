package codedriver.module.tenant.api.systemnotice;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.systemnotice.dao.mapper.SystemNoticeMapper;
import codedriver.framework.systemnotice.dto.SystemNoticeVo;
import codedriver.framework.systemnotice.exception.SystemNoticeNotFoundException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Title: SystemNoticeIssueApi
 * @Package: codedriver.module.tenant.api.systemnotice
 * @Description: 系统公告下发接口
 * @Author: laiwt
 * @Date: 2021/1/13 18:01
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/

@Service
@OperationType(type = OperationTypeEnum.OPERATE)
@Transactional
public class SystemNoticeIssueApi extends PrivateApiComponentBase {

    @Autowired
    private SystemNoticeMapper systemNoticeMapper;

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
        if(oldVo == null){
            throw new SystemNoticeNotFoundException(vo.getId());
        }
        if(SystemNoticeVo.Status.ISSUED.getValue().equals(oldVo.getStatus())){
            //todo 已下发异常
        }
        vo.setStatus(SystemNoticeVo.Status.ISSUED.getValue());
        systemNoticeMapper.updateSystemNotice(vo);
        if(SystemNoticeVo.Status.NOTISSUED.getValue().equals(oldVo.getStatus())){
            // todo 如果没有设置生效时间或者当前时间大于等于生效时间，则发送给在线用户

        }else if(SystemNoticeVo.Status.STOPPED.getValue().equals(oldVo.getStatus())){
            // todo 如果是已停用状态，则发送给其中的在线用户
        }
        return null;
    }
}
