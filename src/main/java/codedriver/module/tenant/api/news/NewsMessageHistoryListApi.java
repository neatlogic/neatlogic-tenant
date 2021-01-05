package codedriver.module.tenant.api.news;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.news.core.NewsHandlerFactory;
import codedriver.framework.news.dao.mapper.NewsMapper;
import codedriver.framework.news.dto.NewsHandlerVo;
import codedriver.framework.news.dto.NewsMessageSearchVo;
import codedriver.framework.news.dto.NewsMessageVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.TimeUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Title: NewsMessageHistoryListApi
 * @Package codedriver.module.tenant.api.news
 * @Description: 查询历史消息列表接口
 * @Author: linbq
 * @Date: 2021/1/4 11:05
 **/
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class NewsMessageHistoryListApi extends PrivateApiComponentBase {

    @Autowired
    private NewsMapper newsMapper;

    @Override
    public String getToken() {
        return "news/message/history/list";
    }

    @Override
    public String getName() {
        return "查询历史消息列表";
    }

    @Override
    public String getConfig() {
        return null;
    }
    @Input({
            @Param(name = "moduleId", type = ApiParamType.STRING, desc = "模块id"),
            @Param(name = "startTime", type = ApiParamType.LONG, desc = "开始时间"),
            @Param(name = "endTime", type = ApiParamType.LONG, desc = "结束时间"),
            @Param(name = "timeRange", type = ApiParamType.INTEGER, desc = "模块id"),
            @Param(name = "timeUnit", type = ApiParamType.ENUM, rule = "year,month,week,day,hour", desc = "模块id"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条数"),
            @Param(name = "needPage", type = ApiParamType.INTEGER, desc = "是否分页")
    })
    @Output({
            @Param(name = "tbodyList", explode = NewsMessageVo[].class, desc = "消息列表"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "查询历史消息列表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        List<NewsMessageVo> newsMessageVoList = new ArrayList<>();
        NewsMessageSearchVo searchVo = JSONObject.toJavaObject(jsonObj, NewsMessageSearchVo.class);
        String moduleId = jsonObj.getString("moduleId");
        if(StringUtils.isNotBlank(moduleId)){
            List<String> handlerList = new ArrayList<>();
            for (NewsHandlerVo newsHandlerVo : NewsHandlerFactory.getNewsHandlerVoList()) {
                if(moduleId.equals(newsHandlerVo.getModuleId())){
                    handlerList.add(newsHandlerVo.getHandler());
                }
            }
            searchVo.setHandlerList(handlerList);
        }
        if(searchVo.getStartTime() == null && searchVo.getEndTime() == null){
            Integer timeRange = jsonObj.getInteger("timeRange");
            String timeUnit = jsonObj.getString("timeUnit");
            if(timeRange != null && StringUtils.isNotBlank(timeUnit)){
                searchVo.setStartTime(TimeUtil.recentTimeTransfer(timeRange, timeUnit));
                searchVo.setEndTime(new Date());
            }
        }
        searchVo.setUserUuid(UserContext.get().getUserUuid(true));
        if(searchVo.getNeedPage()){
            int pageCount = 0;
            int rowNum = newsMapper.getNewsMessageHistoryCount(searchVo);
            if(rowNum > 0){
                pageCount = PageUtil.getPageCount(rowNum, searchVo.getPageSize());
                if(searchVo.getCurrentPage() <= pageCount){
                    newsMessageVoList = newsMapper.getNewsMessageHistoryList(searchVo);
                }
            }
            resultObj.put("currentPage", searchVo.getCurrentPage());
            resultObj.put("pageSize", searchVo.getPageSize());
            resultObj.put("pageCount", pageCount);
            resultObj.put("rowNum", rowNum);
        }else{
            newsMessageVoList = newsMapper.getNewsMessageHistoryList(searchVo);
        }
        resultObj.put("tbodyList", newsMessageVoList);
        return resultObj;
    }
}
