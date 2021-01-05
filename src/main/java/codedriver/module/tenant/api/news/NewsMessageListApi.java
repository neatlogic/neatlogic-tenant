package codedriver.module.tenant.api.news;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.news.dao.mapper.NewsMapper;
import codedriver.framework.news.dto.NewsMessageSearchVo;
import codedriver.framework.news.dto.NewsMessageVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Title: NewsMessageListApi
 * @Package codedriver.module.tenant.api.news
 * @Description: 查询消息列表接口
 * @Author: linbq
 * @Date: 2020/12/31 16:17
 **/
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class NewsMessageListApi extends PrivateApiComponentBase {

    @Autowired
    private NewsMapper newsMapper;

    @Override
    public String getToken() {
        return "news/message/list";
    }
    @Override
    public String getName() {
        return "查询消息列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "newsMessageId", type = ApiParamType.LONG, desc = "起点消息id"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条数"),
            @Param(name = "needPage", type = ApiParamType.INTEGER, desc = "是否分页")
    })
    @Output({
            @Param(name = "tbodyList", explode = NewsMessageVo[].class, desc = "消息列表"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "查询消息列表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        List<NewsMessageVo> newsMessageVoList = new ArrayList<>();
        NewsMessageSearchVo searchVo = JSONObject.toJavaObject(jsonObj, NewsMessageSearchVo.class);
        searchVo.setUserUuid(UserContext.get().getUserUuid(true));
        if(searchVo.getNeedPage()){
            int pageCount = 0;
            int rowNum = newsMapper.getNewsMessageCount(searchVo);
            if(rowNum > 0){
                pageCount = PageUtil.getPageCount(rowNum, searchVo.getPageSize());
                if(searchVo.getCurrentPage() <= pageCount){
                    newsMessageVoList = newsMapper.getNewsMessageList(searchVo);
                }
            }
            resultObj.put("currentPage", searchVo.getCurrentPage());
            resultObj.put("pageSize", searchVo.getPageSize());
            resultObj.put("pageCount", pageCount);
            resultObj.put("rowNum", rowNum);
        }else{
            newsMessageVoList = newsMapper.getNewsMessageList(searchVo);
        }
        resultObj.put("tbodyList", newsMessageVoList);
        int newCount = 0;
        if(CollectionUtils.isNotEmpty(newsMessageVoList)){
            newCount = newsMapper.getNewsMessageNewCount(searchVo);
        }
        resultObj.put("newCount", newCount);
        return resultObj;
    }
}
