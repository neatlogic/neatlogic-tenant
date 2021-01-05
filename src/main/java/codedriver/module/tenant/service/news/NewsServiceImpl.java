package codedriver.module.tenant.service.news;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.news.dao.mapper.NewsMapper;
import codedriver.framework.news.dto.NewsMessageSearchVo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @Title: NewsServiceImpl
 * @Package codedriver.module.tenant.service.news
 * @Description: 消息通知Service实现类
 * @Author: linbq
 * @Date: 2021/1/5 14:26
 **/
@Service
public class NewsServiceImpl implements NewsService {

    @Autowired
    private NewsMapper newsMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TeamMapper teamMapper;
    @Override
    public List<Long> pullNews(NewsMessageSearchVo searchVo) {
        searchVo.setUserUuid(UserContext.get().getUserUuid(true));
        searchVo.setRoleUuidList(userMapper.getRoleUuidListByUserUuid(UserContext.get().getUserUuid(true)));
        searchVo.setTeamUuidList(teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true)));

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - 7);
        Date earliestSendingTime = calendar.getTime();

        Date lastPullTime = null;
        Long maxNewsMessageId = newsMapper.getNewsMessageUserMaxNewsMessageIdByUserUuid(UserContext.get().getUserUuid(true));
        if (maxNewsMessageId != null) {
            lastPullTime = newsMapper.getNewsMessageFcdById(maxNewsMessageId);
        }
        if (lastPullTime == null || lastPullTime.before(earliestSendingTime)) {
            searchVo.setStartTime(earliestSendingTime);
        } else {
            searchVo.setStartTime(lastPullTime);
        }
        if(searchVo.getNeedPage()){
            int rowNum = newsMapper.getNewsMessagePullCount(searchVo);
            searchVo.setRowNum(rowNum);
            if(rowNum > 0){
                searchVo.setPageCount(PageUtil.getPageCount(rowNum, searchVo.getPageSize()));
                List<Long> newsMessageIdList = newsMapper.getNewsMessagePullList(searchVo);
                insertNewsMessageUserList(newsMessageIdList);
                return newsMessageIdList;
            }
        }else{
            List<Long> newsMessageIdList = newsMapper.getNewsMessagePullList(searchVo);
            insertNewsMessageUserList(newsMessageIdList);
            return newsMessageIdList;
        }
        return null;
    }

    /**
     * @Description: 保存用户拉取到的新消息id
     * @Author: linbq
     * @Date: 2021/1/5 14:36
     * @Params:[newsMessageIdList]
     * @Returns:void
     **/
    private void insertNewsMessageUserList(List<Long> newsMessageIdList) {
        int size = Math.min(1000, newsMessageIdList.size());
        List<NewsMessageSearchVo> newsMessageSearchVoList = new ArrayList<>(size);
        String userUuid = UserContext.get().getUserUuid(true);
        for (Long newsMessageId : newsMessageIdList) {
            newsMessageSearchVoList.add(new NewsMessageSearchVo(userUuid, newsMessageId));
            if (newsMessageSearchVoList.size() == 1000) {
                newsMapper.insertNewsMessageUser(newsMessageSearchVoList);
                newsMessageSearchVoList.clear();
            }
        }
        if (CollectionUtils.isNotEmpty(newsMessageSearchVoList)) {
            newsMapper.insertNewsMessageUser(newsMessageSearchVoList);
        }
    }
}
