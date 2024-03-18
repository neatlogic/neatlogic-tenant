
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
