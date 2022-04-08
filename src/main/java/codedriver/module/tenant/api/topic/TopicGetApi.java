package codedriver.module.tenant.api.topic;

import codedriver.framework.dao.mapper.TopicMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author longrf
 * @date 2022/4/8 3:52 下午
 */
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class TopicGetApi extends PrivateApiComponentBase {

    @Resource
    TopicMapper topicMapper;

    @Override
    public String getName() {
        return "获取主题配置";
    }

    @Override
    public String getToken() {
        return "topic/get";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({})
    @Description(desc = "获取主题配置接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        return topicMapper.getTopic();
    }
}
