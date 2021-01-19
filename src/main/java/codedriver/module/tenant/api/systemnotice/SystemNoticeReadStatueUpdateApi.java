package codedriver.module.tenant.api.systemnotice;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.systemnotice.dao.mapper.SystemNoticeMapper;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Title: SystemNoticeReadStatueUpdateApi
 * @Package: codedriver.module.tenant.api.systemnotice
 * @Description: 更新系统公告为已读状态接口
 * @Author: laiwt
 * @Date: 2021/1/13 18:01
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
public class SystemNoticeReadStatueUpdateApi extends PrivateApiComponentBase {

    @Autowired
    private SystemNoticeMapper systemNoticeMapper;

    @Override
    public String getToken() {
        return "systemnotice/readstatus/update";
    }

    @Override
    public String getName() {
        return "更新系统公告已读状态";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "idList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "公告ID列表")})
    @Output({})
    @Description(desc = "更新系统公告已读状态")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray idList = jsonObj.getJSONArray("idList");
        if(CollectionUtils.isNotEmpty(idList)){
            for(Object id : idList){
                systemNoticeMapper.updateSystemNoticeUserReadStatus(Long.valueOf(id.toString()), UserContext.get().getUserUuid(true));
            }
        }
        return null;
    }
}
