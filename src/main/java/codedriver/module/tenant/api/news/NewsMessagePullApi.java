package codedriver.module.tenant.api.news;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.news.constvalue.PopUpType;
import codedriver.framework.news.core.NewsHandlerFactory;
import codedriver.framework.news.dao.mapper.NewsMapper;
import codedriver.framework.news.dto.NewsHandlerVo;
import codedriver.framework.news.dto.NewsMessageSearchVo;
import codedriver.framework.news.dto.NewsMessageVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.service.news.NewsService;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Title: NewsMessagePullApi
 * @Package codedriver.module.tenant.api.news
 * @Description: 拉取新消息列表接口
 * @Author: linbq
 * @Date: 2021/1/4 15:13
 **/
@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@Transactional
public class NewsMessagePullApi extends PrivateApiComponentBase {

    @Autowired
    private NewsMapper newsMapper;
    @Autowired
    private NewsService newsService;

    @Override
    public String getToken() {
        return "news/message/pull";
    }

    @Override
    public String getName() {
        return "拉取新消息列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条数")
    })
    @Output({
            @Param(name = "tbodyList", explode = NewsMessageVo[].class, desc = "消息列表"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "拉取新消息列表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        resultObj.put("tbodyList", new ArrayList<>());
        NewsMessageSearchVo searchVo = JSONObject.toJavaObject(jsonObj, NewsMessageSearchVo.class);
        Map<String, NewsHandlerVo> newsSubscribeMap = new HashMap<>();
        List<String> unActiveHandlerList = new ArrayList<>();
        List<NewsHandlerVo> newsSubscribeList = newsMapper.getNewsSubscribeListByUserUuid(UserContext.get().getUserUuid(true));
        for (NewsHandlerVo newsSubscribe : newsSubscribeList) {
            newsSubscribeMap.put(newsSubscribe.getHandler(), newsSubscribe);
            if(newsSubscribe.getIsActive() == 0){
                unActiveHandlerList.add(newsSubscribe.getHandler());
            }
        }
        if(CollectionUtils.isNotEmpty(unActiveHandlerList)){
            List<String> handlerList = NewsHandlerFactory.getNewsHandlerVoList().stream().map(NewsHandlerVo::getHandler).collect(Collectors.toList());
            handlerList.removeAll(unActiveHandlerList);
            searchVo.setHandlerList(handlerList);
        }
        List<Long> newsMessageIdList = newsService.pullNews(searchVo);
        resultObj.put("currentPage", searchVo.getCurrentPage());
        resultObj.put("pageSize", searchVo.getPageSize());
        resultObj.put("rowNum", searchVo.getRowNum());
        resultObj.put("pageCount", searchVo.getPageCount());
        if(CollectionUtils.isNotEmpty(newsMessageIdList)){
            List<NewsMessageVo> newsMessageVoList = newsMapper.getNewsMessageListByIdList(newsMessageIdList);
            for(NewsMessageVo newsMessageVo : newsMessageVoList){
                newsMessageVo.setIsRead(0);
                NewsHandlerVo newsHandlerVo = newsSubscribeMap.get(newsMessageVo.getHandler());
                if(newsHandlerVo != null){
                    newsMessageVo.setPopUp(newsHandlerVo.getPopUp());
                }else{
                    newsMessageVo.setPopUp(PopUpType.CLOSE.getValue());
                }
            }
            resultObj.put("tbodyList", newsMessageVoList);
        }
        return resultObj;
    }
}
