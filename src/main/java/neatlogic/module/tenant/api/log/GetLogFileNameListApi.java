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
import neatlogic.framework.util.TimeUtil;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

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
                        String instanceName = System.getProperties().getProperty(SystemProperty.INSTANCE_NAME);
                        if (instanceName == null) {
                            instanceName = StringUtils.EMPTY;
                        }
                        String yyyy_MM_dd = new SimpleDateFormat(TimeUtil.YYYY_MM_DD).format(new Date());
                        // 当天的接口访问日志名称
                        String accessLogFileName = instanceName + "." + yyyy_MM_dd + ".acc";
                        Arrays.sort(listFiles, Comparator.comparing(File::getName));
                        for (File file : listFiles) {
                            if (file.isFile()) {
                                String fileName = file.getName();
                                // 接口访问日志每日归档，限制只能访问当天的接口访问日志
                                if (fileName.startsWith(instanceName) && fileName.endsWith(".acc")) {
                                    if (!Objects.equals(fileName, accessLogFileName)) {
                                        continue;
                                    }
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
