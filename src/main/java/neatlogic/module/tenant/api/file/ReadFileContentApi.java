/*
Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package neatlogic.module.tenant.api.file;

import neatlogic.framework.asynchronization.threadlocal.RequestContext;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.FileUtil;
import neatlogic.framework.heartbeat.dao.mapper.ServerMapper;
import neatlogic.framework.heartbeat.dto.ServerClusterVo;
import neatlogic.framework.integration.authentication.enums.AuthenticateType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.HttpRequestUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ReadFileContentApi extends PrivateApiComponentBase {

    private final Logger logger = LoggerFactory.getLogger(ReadFileContentApi.class);

    @Resource
    private ServerMapper serverMapper;

    /*查看审计记录时可显示的最大字节数，超过此数需要下载文件后查看*/
    public final static int MAX_FILE_SIZE = 1024 * 1024;

    @Override
    public String getToken() {
        return "file/content/read";
    }

    @Override
    public String getName() {
        return "读取文件内容";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "filePath", type = ApiParamType.STRING, isRequired = true, desc = "文件路径")
    })
    @Output({
            @Param(name = "content", type = ApiParamType.STRING, desc = "内容"),
            @Param(name = "hasMore", type = ApiParamType.BOOLEAN, desc = "是否还有更多内容")
    })
    @Description(desc = "读取文件内容")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Map<String, String> paramMap = new HashMap<>();
        String filePath = paramObj.getString("filePath");
        String[] split = filePath.split("\\?");
        String path = split[0];
        if (split.length >= 2) {
            String[] paramArray = split[1].split("&");
            for (String param : paramArray) {
                if (param.contains("=")) {
                    String[] paramKeyValue = param.split("=");
                    paramMap.put(paramKeyValue[0], paramKeyValue[1]);
                }
            }
        }
        int startIndex = 0;
        int offset = 0;
        int serverId = 0;
        String startIndexStr = paramMap.get("startIndex");
        if (StringUtils.isNotBlank(startIndexStr)) {
            startIndex = Integer.parseInt(startIndexStr);
        }
        String offsetStr = paramMap.get("offset");
        if (StringUtils.isNotBlank(offsetStr)) {
            offset = Integer.parseInt(offsetStr);
        }
        String serverIdStr = paramMap.get("serverId");
        if (StringUtils.isNotBlank(serverIdStr)) {
            serverId = Integer.parseInt(serverIdStr);
        }
        if (Objects.equals(serverId, Config.SCHEDULE_SERVER_ID)) {
            return readLocalFile(path, startIndex, offset);
        } else {
            return readRemoteFile(paramObj, serverId);
        }
    }

    /**
     * 读取服务器本地文件内容
     * @param path 路径
     * @param startIndex 开始下标
     * @param offset 读取内容字节数
     * @return 文件内容
     */
    private JSONObject readLocalFile(String path, int startIndex, int offset) {
        String dataHome = Config.DATA_HOME() + TenantContext.get().getTenantUuid();
        String prefix = "${home}";
        if (path.startsWith(prefix)) {
            path = path.substring(prefix.length());
            path = dataHome + path;
        }
        if (!path.startsWith("file:")) {
            path = "file:" + path;
        }
        JSONObject resultObj = new JSONObject();
        boolean hasMore = false;
        /*
         * 如果偏移量大于最大字节数限制，那么就只截取最大字节数长度的数据
         */
        if (offset > MAX_FILE_SIZE) {
            offset = MAX_FILE_SIZE;
            hasMore = true;
        }
        resultObj.put("hasMore", hasMore);
        try (InputStream in = FileUtil.getData(path)) {
            if (in != null) {
                in.skip(startIndex);
                byte[] buff = new byte[1024];
                StringBuilder sb = new StringBuilder();
                int len;
                int endPoint = 0;
                while ((len = in.read(buff)) != -1) {
                    endPoint += len;
                    if (endPoint >= offset) {
                        len = (len - (endPoint - offset));
                        sb.append(new String(buff, 0, len, StandardCharsets.UTF_8));
                        break;
                    } else {
                        sb.append(new String(buff, 0, len, StandardCharsets.UTF_8));
                    }
                }
                resultObj.put("content", sb.toString());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return resultObj;
    }

    /**
     * 读取其他服务器文件内容
     * @param paramObj 入参
     * @param serverId 服务器ID
     * @return 文件内容
     */
    private JSONObject readRemoteFile(JSONObject paramObj, Integer serverId) {
        JSONObject resultObj = new JSONObject();
        String host = null;
        TenantContext.get().setUseDefaultDatasource(true);
        ServerClusterVo serverClusterVo = serverMapper.getServerByServerId(serverId);
        if (serverClusterVo != null) {
            host = serverClusterVo.getHost();
        }
        TenantContext.get().setUseDefaultDatasource(false);
        if (StringUtils.isBlank(host)) {
            return resultObj;
        }
        HttpServletRequest request = RequestContext.get().getRequest();
        String url = host + request.getRequestURI();
        HttpRequestUtil httpRequestUtil = HttpRequestUtil.post(url)
                .setPayload(paramObj.toJSONString())
                .setAuthType(AuthenticateType.BUILDIN)
                .setConnectTimeout(5000)
                .setReadTimeout(5000)
                .sendRequest();
        String error = httpRequestUtil.getError();
        if(StringUtils.isNotBlank(error)){
            throw new RuntimeException(error);
        }
        JSONObject resultJson = httpRequestUtil.getResultJson();
        if (MapUtils.isNotEmpty(resultJson)) {
            String status = resultJson.getString("Status");
            if (!"OK".equals(status)) {
                throw new RuntimeException(resultJson.getString("Message"));
            }
            resultObj = resultJson.getJSONObject("Return");
        }
        return resultObj;
    }
}
