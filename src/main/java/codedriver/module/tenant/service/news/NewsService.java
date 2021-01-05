package codedriver.module.tenant.service.news;

import codedriver.framework.news.dto.NewsMessageSearchVo;

import java.util.List;

/**
 * @Title: NewsService
 * @Package codedriver.module.tenant.service.news
 * @Description: 消息通知Service接口
 * @Author: linbq
 * @Date: 2021/1/5 14:25
 **/
public interface NewsService {
    /**
     * @Description: 拉取新消息
     * @Author: linbq
     * @Date: 2021/1/5 14:34
     * @Params:[searchVo] 拉取条件
     * @Returns:java.util.List<java.lang.Long> 返回新消息id列表
     **/
    public List<Long> pullNews(NewsMessageSearchVo searchVo);
}
