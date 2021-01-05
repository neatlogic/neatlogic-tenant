package codedriver.module.tenant.api.news;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.news.constvalue.PopUpType;
import codedriver.framework.news.core.INewsHandler;
import codedriver.framework.news.core.NewsHandlerFactory;
import codedriver.framework.news.dao.mapper.NewsMapper;
import codedriver.framework.news.dto.NewsHandlerVo;
import codedriver.framework.news.exception.NewsHandlerNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Title: NewsHandlerPopUpUpdateApi
 * @Package codedriver.module.tenant.api.news
 * @Description: 更新消息类型桌面推送方式接口
 * @Author: linbq
 * @Date: 2020/12/31 14:55
 **/
@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@Transactional
public class NewsHandlerPopUpUpdateApi extends PrivateApiComponentBase {

    @Autowired
    private NewsMapper newsMapper;

    @Override
    public String getToken() {
        return "news/handler/popup/update";
    }

    @Override
    public String getName() {
        return "更新消息类型桌面推送方式";
    }

    @Override
    public String getConfig() {
        return null;
    }
    @Input({
            @Param(name = "handler", type = ApiParamType.STRING, isRequired = true, desc = "消息类型处理器全类名"),
            @Param(name = "popUp", type = ApiParamType.ENUM, rule = "shortshow,longshow,close", isRequired = true, desc = "桌面推送方式")
    })
    @Description(desc = "更新消息类型桌面推送方式")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String handler = jsonObj.getString("handler");
        INewsHandler newsHandler = NewsHandlerFactory.getHandler(handler);
        if(newsHandler == null){
            throw new NewsHandlerNotFoundException(handler);
        }
        String popUp = jsonObj.getString("popUp");
        NewsHandlerVo searchVo = new NewsHandlerVo();
        searchVo.setHandler(handler);
        searchVo.setUserUuid(UserContext.get().getUserUuid(true));
        NewsHandlerVo newsHandlerVo = newsMapper.getNewsSubscribeByUserUuidAndHandler(searchVo);
        if(newsHandlerVo != null){
            newsHandlerVo.setPopUp(popUp);
            newsHandlerVo.setUserUuid(UserContext.get().getUserUuid(true));
            newsMapper.updateNewsSubscribePopUp(newsHandlerVo);
        }else{
            newsHandlerVo = new NewsHandlerVo();
            newsHandlerVo.setHandler(handler);
            newsHandlerVo.setIsActive(1);
            newsHandlerVo.setPopUp(popUp);
            newsHandlerVo.setUserUuid(UserContext.get().getUserUuid(true));
            newsMapper.insertNewsSubscribe(newsHandlerVo);
        }
        return null;
    }
}
