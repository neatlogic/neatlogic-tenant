package neatlogic.module.tenant.api.logger;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.module.tenant.service.ServerService;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetLoggerStatusApi extends PrivateApiComponentBase {

    @Resource
    private ServerService serverService;
    @Override
    public String getName() {
        return "nmtal.getloggerstatusapi.getname";
    }

    @Override
    public String getToken() {
        return "logger/status";
    }

    @Input({
            @Param(name = "serverId", type = ApiParamType.INTEGER, desc = "term.framework.serverid")
    })
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "common.tbodylist")
    })
    @Description(desc = "nmtal.getloggerstatusapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Integer serverId = paramObj.getInteger("serverId");
        if (serverId == null) {
            serverId = Config.SCHEDULE_SERVER_ID;
        }
        if (Objects.equals(serverId, Config.SCHEDULE_SERVER_ID)) {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            // 获取当前Logback配置状态
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(byteArrayOutputStream);
            StatusPrinter.setPrintStream(printStream);
            StatusPrinter.print(loggerContext);
            StatusPrinter.setPrintStream(System.out);
            String logbackStatus = byteArrayOutputStream.toString();
            String[] split = logbackStatus.split(System.lineSeparator());
            List<String> tbodyList = Arrays.asList(split);
            return TableResultUtil.getResult(tbodyList);
        } else {
            return serverService.postOtherServerApi(paramObj,serverId);
        }
    }
}
