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

package neatlogic.module.tenant.api.log;

import ch.qos.logback.classic.Level;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.RequestContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.constvalue.SystemProperty;
import neatlogic.framework.exception.SystemPropertyNotFoundException;
import neatlogic.framework.exception.core.ApiRuntimeException;
import neatlogic.framework.exception.file.FileNotFoundException;
import neatlogic.framework.exception.server.ServerHostIsBankException;
import neatlogic.framework.exception.server.ServerNotFoundException;
import neatlogic.framework.heartbeat.dao.mapper.ServerMapper;
import neatlogic.framework.heartbeat.dto.ServerClusterVo;
import neatlogic.framework.integration.authentication.enums.AuthenticateType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.HttpRequestUtil;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.*;

@AuthAction(action = ADMIN.class)
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetLogFileNameListApi extends PrivateApiComponentBase {
    private Logger logger = LoggerFactory.getLogger(GetLogFileNameListApi.class);

    @Resource
    private ServerMapper serverMapper;

    @Override
    public String getName() {
        return "nmtal.getlogfilenamelistapi.getname";
    }

    @Input({
            @Param(name = "serverId", type = ApiParamType.INTEGER, isRequired = true, desc = "term.framework.serverid")
    })
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.INTEGER, desc = "common.tbodylist")
    })
    @Description(desc = "nmtal.getlogfilenamelistapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        Integer serverId = paramObj.getInteger("serverId");
        if (Objects.equals(serverId, Config.SCHEDULE_SERVER_ID)) {
            resultObj.put("level", getLoggerLevel(logger).levelStr);
            String log4jHome = System.getProperties().getProperty(SystemProperty.LOG4J_HOME);
            if (log4jHome != null) {
                JSONArray tbodyList = new JSONArray();
                File dir = new File(log4jHome);
                if (dir.exists()) {
                    File[] listFiles = dir.listFiles();
                    if (listFiles != null) {
                        Arrays.sort(listFiles, Comparator.comparing(File::getName));
                        for (File file : listFiles) {
                            if (file.isFile()) {
                                String fileName = file.getName();
                                if (fileName.startsWith("neatlogic.") && fileName.endsWith(".acc")) {
                                    continue;
                                }
                                JSONObject jsonObj = new JSONObject();
                                jsonObj.put("fileName", fileName);
                                jsonObj.put("fileSize", FileUtils.byteCountToDisplaySize(file.length()));
                                tbodyList.add(jsonObj);
                            }
                        }
                    }
                } else {
                    throw new FileNotFoundException(FileNotFoundException.Type.NONEXISTENT, log4jHome);
                }
                resultObj.put("tbodyList", tbodyList);
                return resultObj;
            } else {
                throw new SystemPropertyNotFoundException(SystemProperty.LOG4J_HOME);
            }
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
                        resultObj = resultJson.getJSONObject("Return");
                    }
                } else {
                    throw new ServerHostIsBankException(serverId);
                }
            } else {
                throw new ServerNotFoundException(serverId);
            }
            return resultObj;
        }
    }

    @Override
    public String getToken() {
        return "log/filename/list";
    }

    private Level getLoggerLevel(Logger logger) {
        if (logger.isTraceEnabled()) {
            return Level.TRACE;
        } else if (logger.isDebugEnabled()) {
            return Level.DEBUG;
        } else if (logger.isInfoEnabled()) {
            return Level.INFO;
        } else if (logger.isWarnEnabled()) {
            return Level.WARN;
        } else if (logger.isErrorEnabled()) {
            return Level.ERROR;
        } else {
            return Level.OFF;
        }
    }
}
