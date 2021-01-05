package codedriver.module.tenant.api.news;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.news.constvalue.PopUpType;
import codedriver.framework.news.core.INewsHandler;
import codedriver.framework.news.core.NewsHandlerFactory;
import codedriver.framework.news.dao.mapper.NewsMapper;
import codedriver.framework.news.dto.NewsHandlerVo;
import codedriver.framework.news.dto.NewsMessageSearchVo;
import codedriver.framework.news.exception.NewsHandlerNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.service.news.NewsService;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @Title: NewsHandlerActiveUpdateApi
 * @Package codedriver.module.tenant.api.news
 * @Description: 消息类型订阅接口
 * @Author: linbq
 * @Date: 2020/12/31 14:54
 **/
@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@Transactional
public class NewsHandlerActiveUpdateApi extends PrivateApiComponentBase {

    @Autowired
    private NewsMapper newsMapper;
    @Autowired
    private NewsService newsService;

    @Override
    public String getToken() {
        return "news/handler/active/update";
    }

    @Override
    public String getName() {
        return "消息类型订阅";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "handler", type = ApiParamType.STRING, isRequired = true, desc = "消息类型处理器全类名")
    })
    @Description(desc = "消息类型订阅")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String handler = jsonObj.getString("handler");
        INewsHandler newsHandler = NewsHandlerFactory.getHandler(handler);
        if (newsHandler == null) {
            throw new NewsHandlerNotFoundException(handler);
        }
        NewsHandlerVo searchVo = new NewsHandlerVo();
        searchVo.setHandler(handler);
        searchVo.setUserUuid(UserContext.get().getUserUuid(true));
        NewsHandlerVo newsHandlerVo = newsMapper.getNewsSubscribeByUserUuidAndHandler(searchVo);
        if (newsHandlerVo != null) {
            newsHandlerVo.setUserUuid(UserContext.get().getUserUuid(true));
            newsMapper.updateNewsSubscribeActive(newsHandlerVo);
            if(newsHandlerVo.getIsActive() == 1){
                pullNews(handler);
            }
        } else {
            newsHandlerVo = new NewsHandlerVo();
            newsHandlerVo.setHandler(handler);
            newsHandlerVo.setIsActive(0);
            newsHandlerVo.setPopUp(PopUpType.CLOSE.getValue());
            newsHandlerVo.setUserUuid(UserContext.get().getUserUuid(true));
            newsMapper.insertNewsSubscribe(newsHandlerVo);
            pullNews(handler);
        }
        return null;
    }

    private void pullNews(String handler) {
        NewsMessageSearchVo newsMessageSearchVo = new NewsMessageSearchVo();
        newsMessageSearchVo.setNeedPage(false);
        List<String> handlerList = new ArrayList<>();
        handlerList.add(handler);
        newsMessageSearchVo.setHandlerList(handlerList);
        newsService.pullNews(newsMessageSearchVo);
    }
}