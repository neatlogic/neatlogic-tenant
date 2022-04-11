package codedriver.module.tenant.api.topic;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.TOPIC_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.TopicMapper;
import codedriver.framework.dto.TopicVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author longrf
 * @date 2022/4/8 3:53 下午
 */

@AuthAction(action = TOPIC_MODIFY.class)
@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class TopicSaveApi extends PrivateApiComponentBase {

    @Resource
    TopicMapper topicMapper;

    @Override
    public String getName() {
        return "保存主题配置";
    }

    @Override
    public String getToken() {
        return "topic/save";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "主键id"),
            @Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "主题配置"),
            @Param(name = "logoFileId", type = ApiParamType.LONG, desc = "logo 图片文件id")
    })
    @Description(desc = "用于保存主题配置、还原主题配置")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        TopicVo topicVo = paramObj.toJavaObject(TopicVo.class);
        topicMapper.deleteTopic();
        if (!Objects.isNull(topicVo.getConfig())) {
            topicMapper.insertTopic(topicVo);
        }
        return null;
    }
}
