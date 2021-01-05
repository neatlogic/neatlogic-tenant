package codedriver.module.tenant.api.news;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.news.core.NewsHandlerFactory;
import codedriver.framework.news.dao.mapper.NewsMapper;
import codedriver.framework.news.dto.NewsHandlerVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Title: NewHandlerListApi
 * @Package codedriver.module.tenant.api.news
 * @Description: 查询消息类型列表接口
 * @Author: linbq
 * @Date: 2020/12/30 17:38
 **/
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class NewsHandlerListApi extends PrivateApiComponentBase {

    @Autowired
    private NewsMapper newsMapper;

    @Override
    public String getToken() {
        return "news/handler/list";
    }

    @Override
    public String getName() {
        return "查询消息类型列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "moduleId", type = ApiParamType.STRING, desc = "模块id")
    })
    @Output({
            @Param(explode = NewsHandlerVo[].class)
    })
    @Description(desc = "查询消息类型列表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String moduleId = jsonObj.getString("moduleId");
        List<NewsHandlerVo> resultList = new ArrayList<>();
        List<NewsHandlerVo> newsSubscribeList = newsMapper.getNewsSubscribeListByUserUuid(UserContext.get().getUserUuid(true));
        Map<String, NewsHandlerVo> newsSubscribeMap = newsSubscribeList.stream().collect(Collectors.toMap(e -> e.getHandler(), e -> e));
        for (NewsHandlerVo newsHandlerVo : NewsHandlerFactory.getNewsHandlerVoList()) {
            if(StringUtils.isBlank(moduleId) || moduleId.equals(newsHandlerVo.getModuleId())){
                NewsHandlerVo newsHandler = newsHandlerVo.clone();
                NewsHandlerVo newsSubscribe = newsSubscribeMap.get(newsHandler.getHandler());
                if (newsSubscribe != null) {
                    newsHandler.setIsActive(newsSubscribe.getIsActive());
                    newsHandler.setPopUp(newsSubscribe.getPopUp());
                }
                resultList.add(newsHandler);
            }
        }
        return resultList;
    }
}
