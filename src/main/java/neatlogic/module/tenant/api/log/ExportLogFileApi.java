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

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.FileUtil;
import neatlogic.framework.constvalue.SystemProperty;
import neatlogic.framework.exception.SystemPropertyNotFoundException;
import neatlogic.framework.exception.core.ApiRuntimeException;
import neatlogic.framework.exception.file.FileNotFoundException;
import neatlogic.framework.exception.server.ServerHostIsBankException;
import neatlogic.framework.exception.server.ServerNotFoundException;
import neatlogic.framework.heartbeat.dao.mapper.ServerMapper;
import neatlogic.framework.heartbeat.dto.ServerClusterVo;
import neatlogic.framework.integration.authentication.enums.AuthenticateType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.util.HttpRequestUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Objects;

@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ExportLogFileApi extends PrivateBinaryStreamApiComponentBase {

    @Resource
    private ServerMapper serverMapper;

    @Override
    public String getName() {
        return "nmtal.exportlogfileapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "serverId", type = ApiParamType.INTEGER, isRequired = true, desc = "term.framework.serverid"),
            @Param(name = "fileName", type = ApiParamType.STRING, isRequired = true, desc = "common.path"),
    })
    @Description(desc = "nmtal.exportlogfileapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSONObject resultObj = new JSONObject(new LinkedHashMap<>());
        Integer serverId = paramObj.getInteger("serverId");
        if (Objects.equals(serverId, Config.SCHEDULE_SERVER_ID)) {
            String log4jHome = System.getProperties().getProperty(SystemProperty.LOG4J_HOME);
            if (log4jHome != null) {
                String fileName = paramObj.getString("fileName");
                String path = log4jHome + File.separator + fileName;
                File file = new File(path);
                if (file.exists()) {
                    if (file.isFile()) {
                        if (!path.startsWith("file:")) {
                            path = "file:" + path;
                        }
                        try (InputStream in = FileUtil.getData(path); ) {
                            if (in != null) {
                                try (ServletOutputStream os = response.getOutputStream()) {
                                    response.setContentType("application/octet-stream");
                                    response.setHeader("Content-Disposition", " attachment; filename=\"" + neatlogic.framework.util.FileUtil.getEncodedFileName(fileName) + "\"");
                                    IOUtils.copyLarge(in, os);
                                    os.flush();
                                }
                            }
                        }
                    } else {
                        throw new FileNotFoundException(FileNotFoundException.Type.DIRECTORY, log4jHome);
                    }
                } else {
                    throw new FileNotFoundException(FileNotFoundException.Type.NONEXISTENT, log4jHome);
                }
            } else {
                throw new SystemPropertyNotFoundException(SystemProperty.LOG4J_HOME);
            }
        } else {
            ServerClusterVo serverClusterVo = serverMapper.getServerByServerId(serverId);
            if (serverClusterVo != null) {
                String host = serverClusterVo.getHost();
                if (StringUtils.isNotBlank(host)) {
                    ServletOutputStream os = response.getOutputStream();
                    String url = host + request.getRequestURI();
                    HttpRequestUtil httpRequestUtil = HttpRequestUtil.download(url, "POST", os)
                            .setPayload(paramObj.toJSONString())
                            .setAuthType(AuthenticateType.BUILDIN)
                            .setConnectTimeout(5000)
                            .setReadTimeout(5000)
                            .sendRequest();
                    String error = httpRequestUtil.getError();
                    if (StringUtils.isNotBlank(error)) {
                        throw new ApiRuntimeException(error);
                    }
                } else {
                    throw new ServerHostIsBankException(serverId);
                }
            } else {
                throw new ServerNotFoundException(serverId);
            }
        }
        return resultObj;
    }

    @Override
    public String getToken() {
        return "log/file/export";
    }
}
