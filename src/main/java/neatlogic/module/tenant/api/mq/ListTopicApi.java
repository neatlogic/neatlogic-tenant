/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
