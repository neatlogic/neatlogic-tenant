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

package neatlogic.module.tenant.api.util;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.FileUtil;
import neatlogic.framework.exception.core.ApiRuntimeException;
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

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
public class DownloadLocalFileApi extends PrivateBinaryStreamApiComponentBase {

    @javax.annotation.Resource
    private ServerMapper serverMapper;

    @Override
    public String getName() {
        return "下载服务器文件";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "path", type = ApiParamType.STRING, desc = "路径", isRequired = true),
            @Param(name = "serverId", type = ApiParamType.INTEGER, desc = "服务器ID")
    })
    @Description(desc = "下载服务器文件")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSONObject resultObj = new JSONObject(new LinkedHashMap<>());
        boolean hasServerId = true;
        Integer serverId = paramObj.getInteger("serverId");
        if (serverId == null) {
            hasServerId = false;
            serverId = Config.SCHEDULE_SERVER_ID;
        }
        if (Objects.equals(serverId, Config.SCHEDULE_SERVER_ID)) {
            String fileName = null;
            String path = paramObj.getString("path");
            int index = -1;
            if (path.contains(File.separator)) {
                index = path.lastIndexOf(File.separator);
            } else {
                index = path.lastIndexOf("/");
            }
            if (index != -1) {
                fileName = path.substring(index + 1);
            } else {
                fileName = path;
            }
            InputStream in = null;
            if (path.startsWith("jar:file:")) {
                String locationPattern = null;
                ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
                int index1 = path.lastIndexOf("jar!");
                if (index1 != -1) {
                    String classpath = path.substring(index1 + 4);
                    locationPattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + classpath;
                } else {
                    locationPattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "**/" + fileName;
                }
                Resource[] resources = resolver.getResources(locationPattern);
                for (Resource resource : resources) {
                    if (resource.exists()) {
                        if (Objects.equals(resource.getURL().toString(), path)) {
                            if (resource.isReadable()) {
                                in = resource.getInputStream();
                            } else {
                                resultObj.put("message", "没有权限读取" + path + "文件");
                            }
                            break;
                        }
                    }
                }
                if (in == null) {
                    resultObj.put("message", "文件不存在");
                }
            } else {
                File file = new File(path);
                if (file.exists()) {
                    if (file.isFile()) {
                        if (file.canRead()) {
                            if (!path.startsWith("file:")) {
                                path = "file:" + path;
                            }
                            in = FileUtil.getData(path);
                        } else {
                            resultObj.put("message", "没有权限读取" + path + "文件");
                        }
                    } else {
                        resultObj.put("message", path + "不是文件");
                    }
                } else {
                    resultObj.put("message", "文件不存在");
                }
            }
            if (in != null) {
                if (!hasServerId) {
                    fileName += "_serverId" + serverId;
                }
                response.setContentType("application/octet-stream");
                response.setHeader("Content-Disposition", " attachment; filename=\"" + neatlogic.framework.util.FileUtil.getEncodedFileName(fileName) + "\"");
                ServletOutputStream os = response.getOutputStream();
                IOUtils.copyLarge(in, os);
                os.flush();
                os.close();
                in.close();
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
                    resultObj.put("message", "serverId为" + serverId + "的应用服务器的`server_status`表中对应数据没有配置host");
                }
            } else {
                resultObj.put("message", "找不到serverId为" + serverId + "的应用服务器");
            }
        }
        return resultObj;
    }

    @Override
    public String getToken() {
        return "util/localfile/download";
    }
}
