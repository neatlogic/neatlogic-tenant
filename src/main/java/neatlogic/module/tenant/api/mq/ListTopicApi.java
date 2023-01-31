/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.tenant.api.mq;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.MQ_MODIFY;
import neatlogic.framework.mq.core.TopicFactory;
import neatlogic.framework.mq.dao.mapper.MqTopicMapper;
import neatlogic.framework.mq.dto.TopicVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Service
@AuthAction(action = MQ_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ListTopicApi extends PrivateApiComponentBase {

    @Resource
    private MqTopicMapper mqTopicMapper;

    @Override
    public String getToken() {
        return "/mq/topic/list";
    }

    @Override
    public String getName() {
        return "获取消息队列主题列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({@Param(explode = TopicVo[].class)})
    @Description(desc = "获取消息队列主题列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<TopicVo> topicList = TopicFactory.getTopicList();
        if (CollectionUtils.isNotEmpty(topicList)) {
            List<TopicVo> activeTopicList = mqTopicMapper.getTopicList();
            for (TopicVo topicVo : topicList) {
                Optional<TopicVo> op = activeTopicList.stream().filter(t -> t.getName().equals(topicVo.getName())).findFirst();
                if (op.isPresent()) {
                    topicVo.setIsActive(op.get().getIsActive());
                } else {
                    topicVo.setIsActive(1);
                }
            }
        }
        return topicList;
    }

}
