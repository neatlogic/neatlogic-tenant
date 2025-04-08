/*
 * Copyright (C) 2025  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neatlogic.module.tenant.api.util;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
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
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.util.Objects;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class DownloadLocalFileApi extends PrivateBinaryStreamApiComponentBase {

    @Resource
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
        Integer serverId = paramObj.getInteger("serverId");
        if (serverId == null) {
            serverId = Config.SCHEDULE_SERVER_ID;
        }
        if (Objects.equals(serverId, Config.SCHEDULE_SERVER_ID)) {
            String fileName = null;
            String path = paramObj.getString("path");
            int index = path.lastIndexOf(File.separator);
            if (index != -1) {
                fileName = path.substring(index + 1);
            } else {
                fileName = path;
            }
            fileName += "_serverId" + serverId;
            File file = new File(path);
            if (!file.exists()) {
                throw new ApiRuntimeException("文件不存在");
            }
            if (!file.isFile()) {
                throw new ApiRuntimeException(path + "不是文件");
            }
            if (!path.startsWith("file:")) {
                path = "file:" + path;
            }
            ServletOutputStream os;
            InputStream in;
            in = FileUtil.getData(path);
            if (in != null) {
                response.setContentType("application/octet-stream");
                response.setHeader("Content-Disposition", " attachment; filename=\"" + neatlogic.framework.util.FileUtil.getEncodedFileName(fileName) + "\"");
                os = response.getOutputStream();
                IOUtils.copyLarge(in, os);
                os.flush();
                os.close();
                in.close();
            }
        } else {
            String host = null;
            TenantContext.get().setUseMasterDatabase(true);
            ServerClusterVo serverClusterVo = serverMapper.getServerByServerId(serverId);
            if (serverClusterVo != null) {
                host = serverClusterVo.getHost();
            }
            TenantContext.get().setUseMasterDatabase(false);
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
            }
        }
        return null;
    }

    @Override
    public String getToken() {
        return "util/localfile/download";
    }
}
