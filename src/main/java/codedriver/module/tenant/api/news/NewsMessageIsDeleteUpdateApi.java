package codedriver.module.tenant.api.news;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.news.dao.mapper.NewsMapper;
import codedriver.framework.news.dto.NewsMessageSearchVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Title: NewsMessageIsDeleteUpdateApi
 * @Package codedriver.module.tenant.api.news
 * @Description: 更新消息为已删除接口
 * @Author: linbq
 * @Date: 2021/1/4 23:13
 **/
@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@Transactional
public class NewsMessageIsDeleteUpdateApi extends PrivateApiComponentBase {

    @Autowired
    private NewsMapper newsMapper;

    @Override
    public String getToken() {
        return "news/message/isdelete/update";
    }

    @Override
    public String getName() {
        return "更新消息为已删除";
    }

    @Override
    public String getConfig() {
        return null;
    }
    @Input({
            @Param(name = "newsMessageId", type = ApiParamType.LONG, desc = "消息id")
    })
    @Description(desc = "更新消息为已删除")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long newsMessageId = jsonObj.getLong("newsMessageId");
        newsMapper.updateNewsMessageUserIsDelete(new NewsMessageSearchVo(UserContext.get().getUserUuid(true), newsMessageId));
        return null;
    }
}
