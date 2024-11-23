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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.MQ_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.mq.core.TopicFactory;
import neatlogic.framework.mq.dao.mapper.MqTopicMapper;
import neatlogic.framework.mq.dto.TopicVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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

    @Input({@Param(name = "handler", type = ApiParamType.STRING, desc = "消息队列类型")})
    @Output({@Param(explode = TopicVo[].class)})
    @Description(desc = "获取消息队列主题列表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        TopicVo topicVo = JSON.toJavaObject(jsonObj, TopicVo.class);
        Set<TopicVo> returnSet = new TreeSet<>(Comparator.comparing(TopicVo::getName));
        List<TopicVo> topicList = TopicFactory.getTopicList(topicVo.getHandler());
        List<TopicVo> activeTopicList = mqTopicMapper.searchTopic(topicVo);
        //以数据库中数据为准
        returnSet.addAll(activeTopicList);
        //再补充内置数据
        returnSet.addAll(topicList);
        return returnSet;
    }

}
