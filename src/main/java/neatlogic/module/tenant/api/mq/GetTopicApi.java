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
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.mq.core.TopicFactory;
import neatlogic.framework.mq.dao.mapper.MqTopicMapper;
import neatlogic.framework.mq.dto.TopicVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = MQ_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetTopicApi extends PrivateApiComponentBase {

    @Resource
    private MqTopicMapper mqTopicMapper;

    @Override
    public String getToken() {
        return "/mq/topic/get";
    }

    @Override
    public String getName() {
        return "获取消息队列主题";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "name", type = ApiParamType.STRING, desc = "消息队列唯一标识")})
    @Output({@Param(explode = TopicVo.class)})
    @Description(desc = "获取消息队列主题")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String name = jsonObj.getString("name");
        TopicVo topicVo = TopicFactory.getTopicByName(name);
        TopicVo topicSettingVo = mqTopicMapper.getTopicByName(name);
        if (topicVo == null) {
            topicVo = topicSettingVo;
        } else {
            if (topicSettingVo != null) {
                topicVo.setConfig(topicSettingVo.getConfig());
            }
        }
        return topicVo;
    }

}
