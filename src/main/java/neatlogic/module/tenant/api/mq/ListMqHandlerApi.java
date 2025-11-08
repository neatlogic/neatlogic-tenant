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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.MQ_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.mq.core.IMqHandler;
import neatlogic.framework.mq.core.MqHandlerFactory;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@AuthAction(action = MQ_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ListMqHandlerApi extends PrivateApiComponentBase {


    @Override
    public String getToken() {
        return "/mq/mqhandler/list";
    }

    @Override
    public String getName() {
        return "获取消息队列类型";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "isEnable", type = ApiParamType.BOOLEAN, desc = "是否启用")})
    @Description(desc = "获取消息队列类型")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<IMqHandler> handlerList = MqHandlerFactory.getMqHandlerList();
        Boolean isEnable = jsonObj.getBoolean("isEnable");
        JSONArray returnList = new JSONArray();
        for (IMqHandler handler : handlerList) {
            if (isEnable == null) {
                returnList.add(new JSONObject() {{
                    this.put("value", handler.getName());
                    this.put("text", handler.getLabel());
                }});
            } else if (Objects.equals(isEnable, handler.isEnable())) {
                returnList.add(new JSONObject() {{
                    this.put("value", handler.getName());
                    this.put("text", handler.getLabel());
                }});
            }
        }
        return returnList;
    }

}
