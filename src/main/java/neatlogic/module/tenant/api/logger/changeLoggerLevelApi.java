
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

package neatlogic.module.tenant.api.logger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@OperationType(type = OperationTypeEnum.UPDATE)
public class changeLoggerLevelApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "logger/updatelevel";
    }

    @Override
    public String getName() {
        return "修改日志级别";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "level", type = ApiParamType.ENUM, rule = "ALL,TRACE,DEBUG,INFO,WARN,ERROR,OFF",
        isRequired = true, desc = "日志级别")})
    @Output({@Param(type = ApiParamType.STRING, desc = "当前日志级别")})
    @Description(desc = "修改日志级别接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String level = jsonObj.getString("level");
        LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger logger = loggerContext.getLogger("neatlogic");
        logger.setLevel(Level.toLevel(level));
        return logger.getLevel().levelStr;
    }

}
