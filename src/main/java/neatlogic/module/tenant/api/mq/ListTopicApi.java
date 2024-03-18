/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tenant.api.mq;

import com.alibaba.fastjson.JSONObject;
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
    @Description(desc = "获取消息队列主题列表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<TopicVo> topicList = TopicFactory.getTopicList();
        if (CollectionUtils.isNotEmpty(topicList)) {
            List<TopicVo> activeTopicList = mqTopicMapper.getTopicList();
            for (TopicVo topicVo : topicList) {
                Optional<TopicVo> op = activeTopicList.stream().filter(t -> t.getName().equals(topicVo.getName())).findFirst();
                if (op.isPresent()) {
                    topicVo.setIsActive(op.get().getIsActive());
                    topicVo.setConfig(op.get().getConfig());
                } else {
                    topicVo.setIsActive(1);
                }
            }
        }
        return topicList;
    }

}
