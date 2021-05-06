
package codedriver.module.tenant.api.logger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.FRAMEWOKR_BASE;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@AuthAction(action = FRAMEWOKR_BASE.class)
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
        ch.qos.logback.classic.Logger logger = loggerContext.getLogger("codedriver");
        logger.setLevel(Level.toLevel(level));
        return logger.getLevel().levelStr;
    }

}
