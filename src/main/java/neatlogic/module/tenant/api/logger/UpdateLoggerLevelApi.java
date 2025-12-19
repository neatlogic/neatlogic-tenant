
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

package neatlogic.module.tenant.api.logger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.service.ServerService;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

@Component
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class UpdateLoggerLevelApi extends PrivateApiComponentBase {

    @Resource
    private ServerService serverService;

    @Override
    public String getToken() {
        return "logger/updatelevel";
    }

    @Override
    public String getName() {
        return "nmtal.updateloggerlevelapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "serverId", type = ApiParamType.INTEGER, isRequired = true, desc = "term.framework.serverid"),
            @Param(name = "level", type = ApiParamType.ENUM, rule = "ALL,TRACE,DEBUG,INFO,WARN,ERROR,OFF", isRequired = true, desc = "common.logger.level")
    })
    @Output({
            @Param(type = ApiParamType.STRING, desc = "common.logger.level")
    })
    @Description(desc = "nmtal.updateloggerlevelapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Integer serverId = paramObj.getInteger("serverId");
        if (Objects.equals(serverId, Config.SCHEDULE_SERVER_ID)) {
            String level = paramObj.getString("level");
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            ch.qos.logback.classic.Logger logger = loggerContext.getLogger("neatlogic");
            logger.setLevel(Level.toLevel(level));
            JSONObject resultObj = new JSONObject();
            resultObj.put("level", logger.getLevel().levelStr);
            resultObj.put("serverId", serverId);
            return resultObj;
        } else {
            return serverService.postOtherServerApi(paramObj,serverId);
        }
    }
}
