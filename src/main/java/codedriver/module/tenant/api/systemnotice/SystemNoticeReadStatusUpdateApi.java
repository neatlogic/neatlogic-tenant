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

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Title: SystemNoticeReadStatusUpdateApi
 * @Package: codedriver.module.tenant.api.systemnotice
 * @Description: 标记未读的系统公告为已读接口
 * 可用于【未读公告全部标记为已读】、【标记单个未读公告为已读】、【标记指定范围的公告为已读】
 * @Author: laiwt
 * @Date: 2021/1/13 18:01
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
public class SystemNoticeReadStatusUpdateApi extends PrivateApiComponentBase {

    @Autowired
    private SystemNoticeMapper systemNoticeMapper;

    @Override
    public String getToken() {
        return "systemnotice/readstatus/update";
    }

    @Override
    public String getName() {
        return "标记未读的系统公告为已读";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "idList", type = ApiParamType.JSONARRAY, desc = "公告ID列表(可不传，如此则把所有公告标为已读)")})
    @Output({})
    @Description(desc = "标记未读的系统公告为已读")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray idList = jsonObj.getJSONArray("idList");
        if(CollectionUtils.isNotEmpty(idList)){
            List<Long> list = idList.stream().map(o -> Long.valueOf(o.toString())).collect(Collectors.toList());
            systemNoticeMapper.updateNoticeUserReadStatusByIdList(list, UserContext.get().getUserUuid(true));
        }else{
            systemNoticeMapper.updateNotReadNoticeToReadByUserUuid(UserContext.get().getUserUuid(true));
        }
        return null;
    }
}
