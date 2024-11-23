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
import neatlogic.framework.exception.type.ParamNotExistsException;
import neatlogic.framework.mq.core.TopicFactory;
import neatlogic.framework.mq.dao.mapper.MqTopicMapper;
import neatlogic.framework.mq.dto.TopicVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = MQ_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class SaveTopicApi extends PrivateApiComponentBase {

    @Resource
    private MqTopicMapper mqTopicMapper;

    @Override
    public String getToken() {
        return "/mq/topic/save";
    }

    @Override
    public String getName() {
        return "term.framework.savetopic";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "name", isRequired = true, type = ApiParamType.STRING, desc = "common.uniquename"),
            @Param(name = "label", type = ApiParamType.STRING, desc = "名称"),
            @Param(name = "isActive", type = ApiParamType.INTEGER, desc = "是否激活"),
            @Param(name = "description", type = ApiParamType.STRING, desc = "说明"),
            @Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "common.config")})
    @Description(desc = "term.framework.savetopic")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String name = jsonObj.getString("name");
        TopicVo topicVo = TopicFactory.getTopicByName(name);
        if (topicVo != null) {
            JSONObject config = jsonObj.getJSONObject("config");
            topicVo.setConfig(config);
            mqTopicMapper.saveTopicConfig(topicVo);
            return null;
        } else {
            TopicVo newTopicVo = JSON.toJavaObject(jsonObj, TopicVo.class);
            if (StringUtils.isBlank(newTopicVo.getLabel())) {
                throw new ParamNotExistsException("label");
            }
            mqTopicMapper.saveTopic(newTopicVo);
        }
        return null;
    }

}
