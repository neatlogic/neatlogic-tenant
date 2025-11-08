/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

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
