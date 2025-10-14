package neatlogic.module.tenant.api.logger;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.RequestContext;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.core.ApiRuntimeException;
import neatlogic.framework.exception.server.ServerHostIsBankException;
import neatlogic.framework.exception.server.ServerNotFoundException;
import neatlogic.framework.heartbeat.dao.mapper.ServerMapper;
import neatlogic.framework.heartbeat.dto.ServerClusterVo;
import neatlogic.framework.integration.authentication.enums.AuthenticateType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.HttpRequestUtil;
import neatlogic.framework.util.TableResultUtil;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetLoggerStatusApi extends PrivateApiComponentBase {

    @Resource
    private ServerMapper serverMapper;
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
            String[] split = logbackStatus.split("\r\n");
            List<String> tbodyList = Arrays.asList(split);
            return TableResultUtil.getResult(tbodyList);
        } else {
            ServerClusterVo serverClusterVo = serverMapper.getServerByServerId(serverId);
            if (serverClusterVo != null) {
                String host = serverClusterVo.getHost();
                if (StringUtils.isNotBlank(host)) {
                    HttpServletRequest request = RequestContext.get().getRequest();
                    String url = host + request.getRequestURI();
                    HttpRequestUtil httpRequestUtil = HttpRequestUtil.post(url)
                            .setPayload(paramObj.toJSONString())
                            .setAuthType(AuthenticateType.BUILDIN)
                            .setConnectTimeout(5000)
                            .setReadTimeout(5000)
                            .sendRequest();
                    String error = httpRequestUtil.getError();
                    if (StringUtils.isNotBlank(error)) {
                        throw new ApiRuntimeException(error);
                    }
                    JSONObject resultJson = httpRequestUtil.getResultJson();
                    if (MapUtils.isNotEmpty(resultJson)) {
                        String status = resultJson.getString("Status");
                        if (!"OK".equals(status)) {
                            throw new RuntimeException(resultJson.getString("Message"));
                        }
                        return resultJson.getJSONObject("Return");
                    }
                } else {
                    throw new ServerHostIsBankException(serverId);
                }
            } else {
                throw new ServerNotFoundException(serverId);
            }
            return null;
        }
    }
}
