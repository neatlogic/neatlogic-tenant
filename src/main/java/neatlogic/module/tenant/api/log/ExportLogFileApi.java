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
import neatlogic.framework.exception.server.ServerNotFoundException;
import neatlogic.framework.heartbeat.dao.mapper.ServerMapper;
import neatlogic.framework.heartbeat.dto.ServerClusterVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.binarystream.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.util.FileSafeUtil;
import neatlogic.framework.util.TimeUtil;
import neatlogic.module.tenant.service.ServerService;
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

    @Resource
    private ServerService serverService;

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
        ServerClusterVo serverClusterVo = serverMapper.getServerLockByServerId(serverId);
        if (serverClusterVo == null) {
            throw new ServerNotFoundException(serverId);
        }
        if (Objects.equals(serverId, Config.SCHEDULE_SERVER_ID)) {
            String log4jHome = System.getProperties().getProperty(SystemProperty.LOG4J_HOME);
            if (log4jHome != null) {
                String fileName = paramObj.getString("fileName");
                File file = FileSafeUtil.getDownloadFile(fileName, log4jHome);
                if (file.exists()) {
                    if (file.isFile()) {
                        String path = file.getPath();
                        if (!path.startsWith("file:")) {
                            path = "file:" + path;
                        }
                        try (InputStream in = FileUtil.getData(path);) {
                            if (in != null) {
                                try (ServletOutputStream os = response.getOutputStream()) {
                                    String prefix = serverId + "-" + TimeUtil.yyyymmdd();
                                    if (StringUtils.isNotBlank(serverClusterVo.getIp())) {
                                        prefix = serverClusterVo.getIp() + "-" + prefix;
                                    }
                                    response.setContentType("application/octet-stream");
                                    response.setHeader("Content-Disposition", " attachment; filename=\"" + neatlogic.framework.util.FileUtil.getEncodedFileName(prefix + "-" + file.getName()) + "\"");
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
            ServletOutputStream os = response.getOutputStream();
            String message = serverService.downloadOtherServerApi(paramObj, serverClusterVo, os);
            if (StringUtils.isNotBlank(message)) {
                throw new ApiRuntimeException(message);
            }
        }
        return resultObj;
    }

    @Override
    public String getToken() {
        return "log/file/export";
    }
}
